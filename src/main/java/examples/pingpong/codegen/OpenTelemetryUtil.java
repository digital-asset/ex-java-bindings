package examples.pingpong.codegen;

import com.daml.ledger.api.v1.TraceContextOuterClass.TraceContext;
import io.grpc.ClientInterceptor;
import io.opentelemetry.api.OpenTelemetry;
import io.opentelemetry.api.common.Attributes;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.api.trace.propagation.W3CTraceContextPropagator;
import io.opentelemetry.context.Context;
import io.opentelemetry.context.propagation.ContextPropagators;
import io.opentelemetry.context.propagation.TextMapGetter;
import io.opentelemetry.exporter.jaeger.JaegerGrpcSpanExporter;
import io.opentelemetry.instrumentation.grpc.v1_6.GrpcTelemetry;
import io.opentelemetry.sdk.OpenTelemetrySdk;
import io.opentelemetry.sdk.resources.Resource;
import io.opentelemetry.sdk.trace.SdkTracerProvider;
import io.opentelemetry.sdk.trace.export.BatchSpanProcessor;
import io.opentelemetry.sdk.trace.samplers.Sampler;
import io.opentelemetry.semconv.resource.attributes.ResourceAttributes;

import javax.annotation.Nullable;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class OpenTelemetryUtil {

    private final OpenTelemetry openTelemetry;
    private final Tracer tracer;

    private final GrpcTelemetry grpcTelemetry;

    public OpenTelemetryUtil(String serviceName) {
        this.openTelemetry = createOpenTelemetry(serviceName);
        this.tracer = openTelemetry.getTracer(serviceName);
        this.grpcTelemetry = GrpcTelemetry.create(openTelemetry);
    }

    public Tracer getTracer() {
        return tracer;
    }

    public ClientInterceptor getClientInterceptor() {
        return grpcTelemetry.newClientInterceptor();
    }

    private static OpenTelemetry createOpenTelemetry(String serviceName) {
        final var spanExporter = JaegerGrpcSpanExporter
                .builder()
                .setEndpoint("http://localhost:14250")
                .setTimeout(30, TimeUnit.SECONDS)
                .build();
        final var batchProcessor = BatchSpanProcessor
                .builder(spanExporter) // change batch size and delay if needed
                .build();
        final var attributes = Attributes
                .builder()
                .put(ResourceAttributes.SERVICE_NAME, serviceName)
                .build();
        final var serviceNameResource = Resource.create(attributes);
        final var tracerProvider = SdkTracerProvider
                .builder()
                .addSpanProcessor(batchProcessor)
                .setSampler(Sampler.alwaysOn())
                .setResource(Resource.getDefault().merge(serviceNameResource))
                .build();
        final var contextPropagators = ContextPropagators.create(W3CTraceContextPropagator.getInstance());
        return OpenTelemetrySdk
                .builder()
                .setTracerProvider(tracerProvider)
                .setPropagators(contextPropagators)
                .build();
    }

    Context contextFromDamlTraceContext(TraceContext traceContext) {
        TextMapGetter<TraceContext> getter = new TextMapGetter<>() {
            @Override
            @Nullable
            public String get(@Nullable TraceContext carrier, String key) {
                return carrier == null ? null : toMap(carrier).get(key);
            }
            @Override
            public Iterable<String> keys(TraceContext carrier) {
                return toMap(carrier).keySet();
            }

            private final String TRACEPARENT_HEADER_NAME = "traceparent"; // same as W3CTraceContextPropagator.TRACE_PARENT
            private final String TRACESTATE_HEADER_NAME = "tracestate";   // same as W3CTraceContextPropagator.TRACE_STATE

            private Map<String, String> toMap(TraceContext traceContext) {
                return Map.of(
                        TRACEPARENT_HEADER_NAME,
                        traceContext.getTraceparent().getValue(),
                        TRACESTATE_HEADER_NAME,
                        traceContext.getTracestate().getValue()
                );
            }
        };
        return openTelemetry
                .getPropagators()
                .getTextMapPropagator()
                .extract(Context.root(), traceContext, getter);
    }
}
