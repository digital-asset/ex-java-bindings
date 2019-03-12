// Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
// SPDX-License-Identifier: Apache-2.0

package examples.pingpong.components;

import com.daml.ledger.javaapi.components.LedgerViewFlowable;
import com.daml.ledger.javaapi.components.helpers.CommandsAndPendingSet;
import com.daml.ledger.javaapi.components.helpers.CreatedContract;
import com.daml.ledger.javaapi.data.*;
import com.google.protobuf.Timestamp;
import io.reactivex.Flowable;
import org.checkerframework.checker.nullness.qual.NonNull;
import org.pcollections.HashTreePMap;
import org.pcollections.HashTreePSet;
import org.pcollections.PMap;
import org.pcollections.PSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.util.Collections;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;

public class PingPongBot {

    private final static Logger logger = LoggerFactory.getLogger(PingPongBot.class);

    private final String ledgerId;
    private final String party;
    private final Identifier templateId;
    private final String choice;

    public PingPongBot(String ledgerId, String party, Identifier templateId, String choice) {
        this.ledgerId = ledgerId;
        this.party = party;
        this.templateId = templateId;
        this.choice = choice;
    }

    public Flowable<CommandsAndPendingSet> process(LedgerViewFlowable.LedgerView<ContractInfo> ledgerView) {
        PMap<String, ContractInfo> contracts = ledgerView.getContracts(this.templateId);
        Stream<CommandsAndPendingSet> commandsAndPendingSetStream = contracts.entrySet().stream().map(this::processContract);
        return Flowable.fromIterable(commandsAndPendingSetStream::iterator);
    }

    private CommandsAndPendingSet processContract(Map.Entry<String, ContractInfo> entry) {
        String contractId = entry.getKey();
        ContractInfo contractInfo = entry.getValue();
        long count = contractInfo.getCount();
        String workflowId = contractInfo.getWorkflowId();
        logger.info("{} is exercising {} on {} in workflow {} at count {}", party, this.choice, contractId, workflowId, count);
        Command command = new ExerciseCommand(this.templateId, contractId, this.choice, new Record(Collections.emptyList()));
        SubmitCommandsRequest commands = new SubmitCommandsRequest(
                workflowId,
                "PingPongApp",
                UUID.randomUUID().toString(),
                this.party,
                Timestamp.newBuilder().setSeconds(Instant.EPOCH.toEpochMilli() / 1000).build(),
                Timestamp.newBuilder().setSeconds(Instant.EPOCH.plusSeconds(10).toEpochMilli() / 1000).build(),
                Collections.singletonList(command));
        PMap<Identifier, PSet<String>> pendingSet = HashTreePMap.singleton(this.templateId, HashTreePSet.singleton(contractId));
        return new CommandsAndPendingSet(commands, pendingSet);
    }

    static ContractInfo getContractInfo(CreatedContract createdContract) {
        logger.debug("getContractInfo({})", createdContract);
        Map<String, Value> fields = createdContract.getCreateArguments().getFieldsMap();
        long count = fields.get("count").asInt64().orElseThrow(() -> new IllegalStateException("Count should be of type Int64, found " + fields.get("count").toString())).getValue();
        return new ContractInfo(count, createdContract.getContext().getWorkflowId());
    }

    public static class ContractInfo {
        private final long count;
        private final String workflowId;

        public ContractInfo(long count, @NonNull String workflowId) {
            this.count = count;
            this.workflowId = workflowId;
        }

        public long getCount() {
            return count;
        }

        public String getWorkflowId() {
            return workflowId;
        }

        @Override
        public String toString() {
            return "ContractInfo{" +
                    "count=" + count +
                    ", workflowId='" + workflowId + '\'' +
                    '}';
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ContractInfo that = (ContractInfo) o;
            return count == that.count &&
                    Objects.equals(workflowId, that.workflowId);
        }

        @Override
        public int hashCode() {

            return Objects.hash(count, workflowId);
        }
    }
}
