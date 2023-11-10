Explicit Disclosure with Java Bindings Example
----------------------------------------------

::

  Copyright (c) 2023 Digital Asset (Switzerland) GmbH and/or its affiliates. All rights reserved.
  SPDX-License-Identifier: Apache-2.0.0

This project exemplifies usage of `Explicit Contract Disclosure <https://docs.daml.com/app-dev/explicit-contract-disclosure.html>`_
in Daml client applications built with the `Java Binding library <https://docs.daml.com/app-dev/bindings-java/index.html>`_.

In this example, four parties, each hosted on their own participant (see the topology configuration in `canton_ledger.conf <canton_ledger.conf>`_), are involved in a simplified trade:

- **Bank** issues ``IOU`` as units of cash
- **StockExchange** issues ``Stock`` as on-ledger asset. Additionally, it issues price ticks for the stock as ``PriceQuotation``
- **Seller** which owns a unit of ``Stock`` issued by the **StockExchange**
- **Buyer** which owns an amount of ``IOU`` and wants to trade a part of it at correct market value in exchange of **Seller** s ``Stock``

The Daml model for the involved templates mentioned above is located in ``daml/StockExchange.daml``.

Running the example
===================

#. If you do not have it already, download and unzip `Canton open-source <https://github.com/digital-asset/daml/releases/download/v2.8.0-snapshot.20231109.2/canton-open-source-2.8.0-snapshot.20231109.11490.0.vd02500a6.tar.gz>`_ into a location of your choice.

#. Use the setup script for exposing the bash utility functions in two shell terminal windows

   source setup.sh

#. In one terminal, build the project

   build_example

#. In the other terminal, start the Canton ledger and wait for initialization until the process prints *Canton server initialization DONE*

   start_canton <path_to_canton_installation>

#. In the first terminal, run the example

   run_stock_exchange
