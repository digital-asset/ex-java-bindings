Java Bindings Example
---------------------

::

  Copyright (c) 2019, Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
  SPDX-License-Identifier: Apache-2.0


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

   curl https://get.daml.com | sh -s 0.13.40

#. Build the Java code with `Maven <https://maven.apache.org/>`_ by running::

    mvn compile

#. Start the sandbox by running::

    daml start --sandbox-port 7600

  Note: this will take over your terminal, until you press CTRL-C to kill the
  sandbox. It will also open up your default web browser to show the Navigator,
  which will allow you to observe the contracts getting created by running one
  of the examples (see next step).

#. Run the applications by running the following command, specifying the main class::

    mvn exec:java \
        -Dexec.mainClass=<mainClass> \
        -Dexec.args="localhost 7600"

  where <mainClass> is one of:

  * examples.pingpong.grpc.PingPongGrpcMain
  * examples.pingpong.reactive.PingPongReactiveMain

  depending on which example you wish to run. Note that if you want to run
  multiple examples, you should start a new sandbox for each one, otherwise
  they might interfere.

Example Project -- Ping Pong with gRPC Bindings
-----------------------------------------------

The code for this example is in the package  `examples.pingpong.grpc <src/main/java/examples/pingpong/grpc>`_.

PingPongGrpcMain.java
---------------------

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
----------------------

The core of the application is the method `PingPongProcessor.runIndefinitely() <src/main/java/examples/pingpong/grpc/PingPongProcessor.java#L61-L91>`_.

This method retrieves a gRPC streaming endpoint using the ``GetTransactionsRequest`` request, and then creates a `RxJava <The Underlying Library: RxJava_>`_ ``StreamObserver``, providing implementations of the ``onNext``, ``onError`` and ``onComplete`` observer methods. ``RxJava`` arranges that these methods receive stream events asynchronously.

The method `onNext <src/main/java/examples/pingpong/grpc/PingPongProcessor.java#L74-L76>`_ is the main driver, extracting the transaction list from each ``GetTransactionResponse``, and passing in to  ``processTransaction()`` for processing. This method, and the method ``processTransaction()`` implents the application logic.

`processTransaction() <src/main/java/examples/pingpong/grpc/PingPongProcessor.java#L98-L117>`_ extracts all creation events from the the transaction and passes them to ``processEvent()``. This produces a list of commands to be sent to the ledegr to further the workflow, and these are packages up in a ``Commands`` request and sent to the ledger.

`processEvent() <src/main/java/examples/pingpong/grpc/PingPongProcessor.java#L129-L169>`_ takes a transaction event and turns it into a stream of commands to be sent back to the ledger. To do this, it examines the event for the correct package and template (it's a create of a ``Ping`` or ``Pong`` template) and then looks at the receiving part to decide if this processor should respond. If so, an exercise command for the correct choice is created and returned in a ``Stream``.

In all other cases, an empty ``Stream`` is returned, indication no action is required.

Output
^^^^^^

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

Example Project -- Ping Pong without Reactive Components
--------------------------------------------------------

The code for this example is in the package `examples.pingpong.reactive <src/main/java/examples/pingpong/reactive>`_.

PingPongReactiveMain.java
^^^^^^^^^^^^^^^^^^^^^^^^^

The entry point for the Java code is the main class `PingPongReactiveMain <src/main/java/examples/pingpong/reactive/PingPongReactiveMain.java#L37-L82>`_.
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

PingPongProcessor.runIndefinitely()
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

The core of the application is the method `PingPongProcessor.runIndefinitely() <src/main/java/examples/pingpong/reactive/PingPongProcessor.java#L42-L49>`_.

The ``PingPongProcessor`` queries the transactions first via the ``TransactionsClient``
of the ``DamlLedgerClient``. Then, for each
transaction, it produces ``Commands`` that will be sent to the Ledger via the ``CommandSubmissionClient``
of the ``DamlLedgerClient``.

Output
^^^^^^

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
