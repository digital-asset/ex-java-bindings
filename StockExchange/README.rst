Example of Explicit Disclosure with Java Bindings
----------------------------------------------

::

  Copyright (c) 2023 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
  SPDX-License-Identifier: Apache-2.0.0

This project demonstrates the usage of `Explicit Contract Disclosure <https://docs.daml.com/app-dev/explicit-contract-disclosure.html>`_
in Daml client applications built with the `Java Binding library <https://docs.daml.com/app-dev/bindings-java/index.html>`_.

In this example, four parties, each hosted on their own participant (see the topology configuration in `canton_ledger.conf <canton_ledger.conf>`_), are involved in a simplified trade.
Each party interacts with the Canton ledger via a standalone Java application. The example interaction flow is modelled as follows:

- Party **Bank** (see `Bank <src/main/java/examples/stockexchange/parties/Bank.java>`_) issues ``IOU`` as units of cash to the **Buyer**
- Party **StockExchange** (see `StockExchange <src/main/java/examples/stockexchange/parties/StockExchange.java>`_) issues ``Stock`` as on-ledger asset to the **Seller** party.
  Additionally, it issues price ticks for the stock as ``PriceQuotation``. Since **StockExchange** is the sole stakeholder of the ``PriceQuotation``,
  it discloses the contract for usage as reference data in commands requiring it as an input.
- Then, party **Seller** (see `Seller <src/main/java/examples/stockexchange/parties/Seller.java>`_) owns a unit of ``Stock`` issued by the **StockExchange**.
  **Seller** creates an ``Offer`` contract on-ledger that can be accepted by any interested party (see ``Offer_Accept`` in the Daml model).
  Similarly to the **StockExchange**, the **Seller** discloses its ``Stock`` and ``Offer`` contracts off-ledger
  for interested parties.
- **Buyer** (see `Buyer <src/main/java/examples/stockexchange/parties/Buyer.java>`_) owns an amount of ``IOU`` issued by **Bank**.
  **Buyer** wants to exchange with the **Seller** and accepts its ``Offer`` on-ledger at the correct ``IOU`` market value in exchange of **Seller** s ``Stock``.
  In the command submission that exercises ``Offer_Accept``, the **Buyer** uses contracts previously disclosed by the **Seller** and **StockExchange**.

**Note**: For illustration, the disclosed contracts in this project are shared via files.
(see `Common.shareDisclosedContract <src/main/java/examples/stockexchange/Common.java>`_).

The Daml model for the templates involved is located in `daml/StockExchange.daml <daml/StockExchange.daml>`_`.

For a better understanding of the explicit disclosure concept and off-ledger data sharing, refer to the
`Explicit Contract Disclosure <https://docs.daml.com/app-dev/explicit-contract-disclosure.html>`_ documentation
where this example's flow is also presented in more detail.

Running the example
===================

#. If you do not have it already, download and unzip `Canton open-source <https://github.com/digital-asset/daml/releases/download/v2.8.0/canton-open-source-2.8.0.tar.gz>`_  or a later version into a location of your choice.

#. Use the setup script for exposing the bash example utility functions in two shell terminal windows

   source setup.sh

#. In one terminal, build the project

   build_example

#. In the other terminal, start the Canton ledger and wait for initialization until the process prints *Canton server initialization DONE*

   start_canton <path_to_canton_installation>

#. In the first terminal, run the example

   run_stock_exchange
