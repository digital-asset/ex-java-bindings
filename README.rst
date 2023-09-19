Java Bindings Example
---------------------

::

  Copyright (c) 2023 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
  SPDX-License-Identifier: Apache-2.0.0


This is an example of how a Java application would use the `Java Binding library <https://docs.daml.com/app-dev/bindings-java/index.html>`_ to connect to and exercise a DAML model running on a ledger. Since there are three levels of interface available, this example builds a similar application with all three levels.

The application is a simple ``PingPong`` application, which consists of:

- a DAML model with two contract templates, ``Ping`` and ``Pong``
- two parties, ``Alice`` and ``Bob``

The logic of the application is the following:

#. The application injects a contract of type ``Ping`` for ``Alice``.
#. ``Alice`` sees this contract and exercises the consuming choice ``RespondPong`` to create a contract
   of type ``Pong`` for ``Bob``.
#. ``Bob`` sees this contract and exercises the consuming choice ``RespondPing``  to create a contract
   of type ``Ping`` for ``Alice``.
#. Points 1 and 2 are repeated until the maximum number of contracts defined in the DAML is
   reached.

Setting Up the Example Projects
-------------------------------

To set a project up:

#. If you do not have it already, install the DAML SDK by running::

   curl https://get.daml.com | sh -s 2.1.0

#. Use the start script for starting a ledger & the java application:

  ./start.sh <mainClass>

  * examples.pingpong.grpc.PingPongGrpcMain
  * examples.pingpong.reactive.PingPongReactiveMain
  * examples.pingpong.codegen.PingPongCodegenMain

  depending on which example you wish to run. The script will take care of stopping an already running sandbox & start a fresh one on every call.

Example Project -- Ping Pong with gRPC Bindings
-----------------------------------------------

The code for this example is in the package  `examples.pingpong.grpc <src/main/java/examples/pingpong/grpc>`_.

PingPongGrpcMain.java
=====================

The entry point for the Java code is the main class `PingPongGrpcMain <src/main/java/examples/pingpong/grpc/PingPongGrpcMain.java#L46-L99>`_. Look at this class to see:

- how to connect to and interact with the DAML Ledger via the Java Binding library
- how to use the gRPC layer to build an automation for both parties.

The main function:

- creates an instance of a ``ManagedChannel`` connecting to an existing ledger
- fetches the ledgerID and packageId from the ledger
- creates ``Identifiers`` for the Ping and Pong templates
- creates and starts instances of `PingPongProcessor <src/main/java/examples/pingpong/grpc/PingPongProcessor.java>`_ that contain the logic of the automation
- injects the initial contracts to start the process

PingPongProcessor.java
======================

The core of the application is the method `PingPongProcessor.runIndefinitely() <src/main/java/examples/pingpong/grpc/PingPongProcessor.java#L61-L91>`_.

This method retrieves a gRPC streaming endpoint using the ``GetTransactionsRequest`` request, and then creates a `RxJava <The Underlying Library: RxJava_>`_ ``StreamObserver``, providing implementations of the ``onNext``, ``onError`` and ``onComplete`` observer methods. ``RxJava`` arranges that these methods receive stream events asynchronously.

The method `onNext <src/main/java/examples/pingpong/grpc/PingPongProcessor.java#L72-L74>`_ is the main driver, extracting the transaction list from each ``GetTransactionResponse``, and passing in to  ``processTransaction()`` for processing. This method, and the method ``processTransaction()`` implements the application logic.

`processTransaction() <src/main/java/examples/pingpong/grpc/PingPongProcessor.java#L96-L115>`_ extracts all creation events from the the transaction and passes them to ``processEvent()``. This produces a list of commands to be sent to the ledger to further the workflow, and these are packages up in a ``Commands`` request and sent to the ledger.

`processEvent() <src/main/java/examples/pingpong/grpc/PingPongProcessor.java#L127-L166>`_ takes a transaction event and turns it into a stream of commands to be sent back to the ledger. To do this, it examines the event for the correct package and template (it's a create of a ``Ping`` or ``Pong`` template) and then looks at the receiving part to decide if this processor should respond. If so, an exercise command for the correct choice is created and returned in a ``Stream``.

In all other cases, an empty ``Stream`` is returned, indication no action is required.

Output
======

The application prints statements similar to these:

.. code-block:: text

    Bob is exercising RespondPong on #1:0 in workflow Ping-Alice-1 at count 0
    Alice is exercising RespondPing on #344:1 in workflow Ping-Alice-7 at count 9

The first line shows that:

- ``Bob`` is exercising the ``RespondPong`` choice on the contract with ID ``#1:0`` for the workflow ``Ping-Alice-1``.
- Count ``0`` means that this is the first choice after the initial ``Ping`` contract.
- The workflow ID  ``Ping-Alice-1`` conveys that this is the workflow triggered by the second initial ``Ping``
  contract that was created by ``Alice``.

The second line is analogous to the first one.

Example Project -- Ping Pong with Reactive Components
-----------------------------------------------------

The code for this example is in the package `examples.pingpong.reactive <src/main/java/examples/pingpong/reactive>`_.

PingPongReactiveMain.java
=========================

The entry point for the Java code is the main class `PingPongReactiveMain <src/main/java/examples/pingpong/reactive/PingPongReactiveMain.java#L34-L80>`_.
Look at this class to see:

- how to connect to and interact with the DAML Ledger via the Java Binding library
- how to use the Reactive layer to build an automation for both parties.

At high level, the code does the following steps:

- creates an instance of ``DamlLedgerClient`` connecting to an existing Ledger
- connect this instance to the Ledger with ``DamlLedgerClient.connect()``
- create two instances of `PingPongProcessor <src/main/java/examples/pingpong/reactive/PingPongProcessor.java>`_, which contain the logic of the automation
- run the ``PingPongProcessor`` forever by connecting them to the incoming transactions
- inject some contracts for each party of both templates
- wait until the application is done

PingPongProcessor.runIndefinitely
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The core of the application is the method `PingPongProcessor.runIndefinitely() <src/main/java/examples/pingpong/reactive/PingPongProcessor.java#L43-L49>`_.

The ``PingPongProcessor`` queries the transactions first via the ``TransactionsClient``
of the ``DamlLedgerClient``. Then, for each
transaction, it produces ``Commands`` that will be sent to the Ledger via the ``CommandSubmissionClient``
of the ``DamlLedgerClient``.

Output
======

The application prints statements similar to these:

.. code-block:: text

    14:36:24.789 [client-1] INFO  e.p.reactive.PingPongProcessor - Bob is exercising RespondPong on #3136:0 in workflow Ping-Alice-1 at count 0
    14:36:24.791 [client-0] INFO  e.p.reactive.PingPongProcessor - Alice is exercising RespondPing on #3139:1 in workflow Ping-Alice-0 at count 1

The Underlying Library: RxJava
==============================

The Java Binding is `RxJava <http://github.com/ReactiveX/RxJava>`_, a library for
composing asynchronous and event-based programs using observable sequences for the Java VM.
It is part of the family of libraries called `ReactiveX <http://reactivex.io/>`_.

ReactiveX was chosen as the underlying library for the Java Binding because
many services that the DAML Ledger offers are exposed as streams of events.
So an application that wants to interact with the DAML Ledger must react
to one or more DAML Ledger streams.

Example Project -- Ping Pong with Generated Java Data Layer
-----------------------------------------------------------

The code for this example is in the package  `examples.pingpong.codegen <src/main/java/examples/pingpong/codegen>`_.

PingPongCodegenMain.java
========================

The entry point for the Java code is the main class `PingPongCodegenMain <src/main/java/examples/pingpong/codegen/PingPongCodegenMain.java#L35-L77>`_. Look at this class to see:

- how to connect to and interact with the DAML Ledger via the Java Binding library
- how to use the gRPC layer to build an automation for both parties.
- how to streamline interactions with the ledger types by using auto generated data layer.

The main function:

- creates an instance of a ``ManagedChannel`` connecting to an existing ledger
- fetches the ledgerID and packageId from the ledger
- creates ``Identifiers`` for the Ping and Pong templates
- creates and starts instances of `PingPongProcessor <src/main/java/examples/pingpong/codegen/PingPongProcessor.java>`_ that contain the logic of the automation
- injects the initial contracts to start the process

PingPongProcessor.java
======================

The core of the application is the method `PingPongProcessor.runIndefinitely() <src/main/java/examples/pingpong/codegen/PingPongProcessor.java#L53-L87>`_.

This method retrieves a gRPC streaming endpoint using the ``GetTransactionsRequest`` request, and then creates a `RxJava <The Underlying Library: RxJava_>`_ ``StreamObserver``, providing implementations of the ``onNext``, ``onError`` and ``onComplete`` observer methods. ``RxJava`` arranges that these methods receive stream events asynchronously.

The method `onNext <src/main/java/examples/pingpong/codegen/PingPongProcessor.java#L70-L72>`_ is the main driver, extracting the transaction list from each ``GetTransactionResponse``, and passing in to  ``processTransaction()`` for processing. This method, and the method ``processTransaction()`` implements the application logic.

`processTransaction() <src/main/java/examples/pingpong/codegen/PingPongProcessor.java#L94-L110>`_ extracts all creation events from the the transaction and passes them to ``processEvent()``. This produces a list of commands to be sent to the ledger to further the workflow, and these are packages up in a ``Commands`` request and sent to the ledger.

`processEvent() <src/main/java/examples/pingpong/codegen/PingPongProcessor.java#L122-L154>`_ takes a transaction event and turns it into a stream of commands to be sent back to the ledger. To do this, it examines the event for the correct package and template (it's a create of a ``Ping`` or ``Pong`` template) and then looks at the receiving part to decide if this processor should respond. If so, an exercise command for the correct choice is created and returned in a ``Stream``.

In all other cases, an empty ``Stream`` is returned, indication no action is required.

Output
======

The application prints statements similar to these:

.. code-block:: text

    Bob is exercising RespondPong on #1:0 in workflow Ping-Alice-1 at count 0
    Alice is exercising RespondPing on #344:1 in workflow Ping-Alice-7 at count 9

The first line shows that:

- ``Bob`` is exercising the ``RespondPong`` choice on the contract with ID ``#1:0`` for the workflow ``Ping-Alice-1``.
- Count ``0`` means that this is the first choice after the initial ``Ping`` contract.
- The workflow ID  ``Ping-Alice-1`` conveys that this is the workflow triggered by the second initial ``Ping``
  contract that was created by ``Alice``.

The second line is analogous to the first one.

The Generated Data Layer
========================

The ``codegen`` variant of the client application is similar to its ``grpc`` counterpart. Both are written in
a traditional imperative style. What sets them apart is the usage of the generated data layer in the former.
This layer simplifies construction of the ledger api calls and the analysis of the return values.

- ``PingPongCodegenMain.createInitialContracts`` creates a strongly typed instance of a Ping contract and then embeds it in an equally strongly typed ``CommandsSubmission``. Then, it uses the built in ``toProto`` methods to convert the request into a wire-ready ``protobuf`` structure.
- ``PingPongProcessor.runIndefinitely`` creates a per party inclusive filter by invoking a series of class constructors. Contrast this with the intricate process of defining a filter in the analogous method in the ``grpc`` variant of the application.
- ``PingPongProcessor.processEvent`` starts off by extracting common data fields from the ``grpc`` version of the received events, to be later used for logging purposes. Events are then converted to the corresponding data layer format and passed to the individual template handlers.
- ``PingPongProcessor.processPingPong`` creates a strongly typed representation of the daml contracts by means of the daml contract companions. A strongly typed instance can be used to create a command representing a desired choice exercise.
- ``PingPongProcessor.processTransaction`` is responsible for creating a ledger request enveloping the choice exercises and submitting it to the ledger.
