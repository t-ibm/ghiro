# Infrastructure as a Service Styles for Centralized and Decentralized Applications
#### Abstract
The applications hosted by a decentralized infrastructure are not materially different from those hosted by a
centralized infrastructure; the services provided by a decentralized infrastructure are similar in spirit to
centralized infrastructure services.

Styles are a mechanism for categorizing architectures and for defining their common characteristics [1]. In
this document we introduce a new system that requires the user to decide between two Infrastructure as a Service
(IaaS) styles, a centralized versus or a decentralized system. The system in turn will then configure and deploy
the _core infrastructure_ and _domain-specific software_ so that the user application either runs as a centralized
or decentralized application.

Decentralized systems don't need to be decentralized end-to-end, significant parts of the infrastructure can
remain centralized. This has a great effect on efficiency since centralized infrastructure is usually significantly
faster and cheaper compared to decentralized. This also means that there is a need to integrate between the
decentralized core infrastructure and domain-specific software.

By the means of _virtualization_ we ensure that independent applications, centralized or decentralized, are hosted
by a dedicated instance of such a system, greatly improving efficiency.

#### Core Infrastructure
The first-class services offered by a core IaaS system are:
- Compute
- Storage
- Messaging
- Consensus (decentralized only)

#### Domain-Specific Software
The core infrastructure only provides the most basic infrastructure, however complex applications require
domain-specific software as well. Examples are:
- Business Intelligence
- Enterprise Resource Planning
- Customer Relationship Management

Such domain-specific software is often built by third parties and is often available only as a centralized
version. There are essentially two options to make it available to a decentralized application:
- by the third-party provider as a decentralized version
- via an integration point

#### Virtualization
Virtualization provides a dedicated instance for 1..n related applications running on top of a shared physical
node.
---
1. E. Di Nitto and D. Rosenblum. Exploiting ADLs to specify architectural styles induced by middleware infrastructures. In Proceedings of the 1999 International Conference on Software Engineering, Los Angeles, May 16-22, 1999, pp. 13-22.
