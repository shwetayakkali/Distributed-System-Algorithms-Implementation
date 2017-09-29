# Distributed-System-Algorithms-Implementation
Algorithms for implementation of Clock Synchronization, Consistency, Distributed Mutual Exclusion, Leader Election

Clock Synchronization : Implementation of Vector Time stamp in a network of 4 servers for a transaction system where each process like checking balance, deposit or withdrawal is a job and it is synchronized based on arrival of request within the network. to manage the order of requests, vector time is used for co-odinationg time among the different servers.
VectorClockThreads.java : Impementation of Clock Synchronization.

Consistency: Implementation of Chandy-Lamport Algorithm for ensuring channel consistency during a snapshot of the network. Follows, the transaction process. Snapshot taken at regular intervals and balance is consistent between the netork of 3 servers.
ChandyLamportSnapshot: Implementation of Chandy Lamport Algorithm for channel consistency.

Mutual Exclusion: Implementation of Token-based Raymond's Algorithm for mutual exclusion. A process can execute a task only whn it has the token, if it doesn't have the token, it passes a request to it's parent in the tree and so on till the node which has the token is reached. Similarly, other nodes also can initial a token request, token access is granted in a FIFO order. On receiving the token the node enters critical section and a task is performed and once task is completed token access is granted to the node which requested it first in the queue but if no node has requested for a token, the token will be present with the node. Node can enter critical section only if it has the token.
RaymondsDME.java - Implementation of Distributed Mutual Exclusion in a network of 7 servers in the form of a tree structure.

Leader Election in Distributed Systems - Implementation of Bully Algorithm for Leader election. All the servers have a priority. The server with the highest priority is elected as the leader. A heart-beat process is followed wherein all the other servers in the network keep pinging the leader to ensure if the leader is up and running. If a leader fails, which is detected by the heart-beat algorithm, a new leader election process starts. If a new server joins the network again and election process starts. 
Bully.java - Implementation of Bully Algorithm for leader election in  network of 5 servers.

