# Distributed Transactional Bank Server

Distributed transactional system that stores bank account balances in multiple servers and allows atomic transfers.

The database server provide deposit and withdrawal transactions for the client, through a Java RMI interface. Each database server is able to be reset at any time and ensure serialization and data consistency.

The transaction server provide operations so that the client application can start and end a transaction (transfer) in a distributed manager.
