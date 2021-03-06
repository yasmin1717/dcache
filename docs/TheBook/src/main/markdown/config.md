Part II. Configuration of dCache
================================

This part contains descriptions of the components of dCache, their role, functionality within the framework. In short, all information necessary for configuring them.

Table of Contents
-----------------

1. [ZooKeeper](config-zookeeper.md)
    - [Deployment scenarios](config-zookeeper.md#deployment-scenarios)
    - [Configuration](config-zookeeper.md#configuration)
    - [Inspecting ZooKeeper through dCache](config-zookeeper.md#inspecting-zooKeeper-through-dcache)

1. [Chimera](config-chimera.md)
    - [Mounting Chimera through NFS](config-chimera.md#mounting-chimera-through-nfs)
    - [Using dCap with a mounted file system](config-chimera.md#using-dcap-with-a-mounted-file-system)
    - [Communicating with Chimera](config-chimera.md#communicating-with-chimera)
    - [IDs](config-chimera.md#ids)
    - [Directory Tags](config-chimera.md#directory-tags)
    - [Create, List and Read Directory Tags if the Namespace is not Mounted](config-chimera.md#create-list-and-read-directory-tags-if-the-namespace-is-not-mounted)
    - [Create, List and Read Directory Tags if the Namespace is Mounted](config-chimera.md#config-chimera.md#create-list-and-read-directory-tags-if-the-namespace-is-mounted)
    - [Directory Tags and Command Files](config-chimera.md#directory-tags-and-command-files)
    - [Directory Tags for dCache](config-chimera.md#directory-tags-for-dcache)
    - [Storage Class and Directory Tags](config-chimera.md#storage-class-and-directory)

1. [Cell Message passing](config-message-passing.md#the-cell-package)

1. [The Resilience Service](config-resilience.md)
    - [Configuring the Resilience Service](config-resilience.md#configuring-the-resilience-service)
    - [Resilience Home](config-resilience.md#resilience-home)
    - [Admin Commands](config-resilience.md#admin-commands)
    - [Tuning](config-resilience.md#tuning)
    - [Resilience's View of Pool Status](config-resilience.md#resilience-s-view-of-pool-status)
    - [Automatic Staging of Missing CUSTODIAL Replicas](config-resilience.md#automatic-staging-of-missing-custoidial-replicas)
    - [Some typical scenarios part 1: what happens when ...?](config-resilience.md#some-typical-scenarios-part-1--what-happens-when-?)
    - [Some typical scenarios part 2: how do I ...?](config-resilience.md#some-typical-scenarios-part-2--how do i-?)

1. [The poolmanager Service](config-PoolManager.md)
    - [The Pool Selection Mechanism](config-PoolManager.md#the-pool-selection-mechanism)
    - [Links](config-PoolManager.md#links)
    - [Examples](config-PoolManager.md#examples)
    - [The Partition Manager](config-PoolManager.md#the-partition-manager)
    - [Overview](config-PoolManager.md#overview)
    - [Managing Partitions](config-PoolManager.md#managing-partitions)
    - [Using Partitions](config-PoolManager.md#using-partitions)
    - [Classic Partitions](config-PoolManager.md#classic-partitions)
    - [Link Groups](config-PoolManager.md#link-groups)

1. [The dCache Tertiary Storage System Interface](config-hsm.md#the-dcache-tertiary-storage-system-interface)
    - [Introduction](config-hsm.md#introduction)
    - [Scope of this chapter](config-hsm.md#scope-of-this-chapter)
    - [Requirements for a Tertiary Storage System](config-hsm.md#requirements-for-a-tertiary-storage-system)
    - [Migrating Tertiary Storage Systems with a file system interface](config-hsm.md#migrating-tertiary-storage-systems-with-a-file-system-interface)
    - [Tertiary Storage Systems with a minimalistic PUT, GET and REMOVE interface](config-hsm.md#tertiary-storage-systems-with-a-minimalistic-put-get-and-remove-interface)
    - [How dCache interacts with a Tertiary Storage System](config-hsm.md#how-dcache-interacts-with-a-tertiary-storage-system)
    - [Details on the TSS-support executable](config-hsm.md#details-on-the-tss-support-executable)
    - [Summary of command line options](config-hsm.md#summary-of-command-line-options)
    - [Summary of return codes](config-hsm.md#summary-of-return-codes)
    - [The executable and the STORE FILE operation](config-hsm.md#the-executable-and-the-store-file-operation)
    - [The executable and the FETCH FILE operation](config-hsm.md#the-executable-and-the-fetch-file-operation)
    - [The executable and the REMOVE FILE operation](config-hsm.md#the-executable-and-the-remove-file-operation)
    - [Configuring pools to interact with a Tertiary Storage System](config-hsm.md#configuring-pools-to-interact-with-a-tertiary-storage-system)
    - [The dCache layout files](config-hsm.md#the-dcache-layout-files)
    - [What happens next](config-hsm.md#what-happens-next)
    - [How to Store-/Restore files via the Admin Interface](config-hsm.md#how-to-store-restore-files-via-the-admin-interface)
    - [How to monitor what’s going on](config-hsm.md#how-to-monitor-whats-going-on)
    - [Log Files](config-hsm.md#log-files)
    - [Obtain information via the dCache Command Line Admin Interface](config-hsm.md#obtain-information-via-the-dcache-command-line-admin-interface)
    - [Example of an executable to simulate a tape backend](config-hsm.md#example-of-an-executable-to-simulate-a-tape-backend)

1. [File Hopping](config-hopping.md)
    - [File Hopping on arrival from outside dCache](config-hopping.md#file-hopping-on-arrival-from-outside-dcache)
    - [File mode of replicated files](config-hopping.md#file-mode-of-replicated-files)
    - [File Hopping managed by the PoolManager](config-hopping.md#file-hopping-managed-by-the-poolmanager)
    - [File Hopping managed by the HoppingManager](config-hopping.md#file-hopping-managed-by-the-hoppingmanager)

1. [Authorization in dCache](config-gplazma.md)
    - [Basics](config-gplazma.md#basics)
    - [Configuration](config-gplazma.md#configuration)
    - [Plug-ins](config-gplazma.md#plug-ins)
    - [Using X.509 Certificates](config-gplazma.md#using-x509-certificates)
    - [CA Certificates](config-gplazma.md#ca-certificates)
    - [User Certificate](config-gplazma.md#user-certificate)
    - [Host Certificate](config-gplazma.md#host-certificate)
    - [VOMS Proxy Certificate](config-gplazma.md#voms-proxy-certificate)
    - [Configuration files](config-gplazma.md#configuration-files)
    - [storage-authzdb](config-gplazma.md#storage-authzdb)
    - [The gplazmalite-vorole-mapping plug-in](config-gplazma.md#the-gplazmalite-vorole-mapping-plug-in)
    - [Authorizing a VO](config-gplazma.md#authorizing-a-vo)
    - [The kpwd plug-in](config-gplazma.md#the-kpwd-plug-in)
    - [The gridmap plug-in](config-gplazma.md#the-gridmap-plug-in)
    - [gPlazma specific dCache configuration](config-gplazma.md#gplazma-specific-dcache-configuration)
    - [Enabling Username/Password Access for WebDAV](config-gplazma.md#enabling-usernamepassword-access-for-webdav)
    - [gPlazma config example to work with authenticated webadmin](config-gplazma.md#gplazma-config-example-to-work-with-authenticated-webadmin)

1. [dCache as xRootd-Server](config-xrootd.md)
    - [Setting up](config-xrootd.md#setting-up)
    - [Parameters](config-xrootd.md#parameters)
    - [Quick tests](config-xrootd.md#quick-tests)
    - [Copying files with xrdcp](config-xrootd.md#copying-files-with-xrdcp)
    - [Accessing files from within ROOT](config-xrootd.md#accessing-files-from-within-root)
    - [xrootd security](config-xrootd.md#xrootd-security)
    - [Read-Write access](config-xrootd.md#read-write-access)
    - [Permitting read/write access on selected directories](config-xrootd.md#permitting-readwrite-access-on-selected-directories)
    - [Token-based authorization](config-xrootd.md#token-based-authorization)
    - [Strong authentication](config-xrootd.md#strong-authentication)
    - [Precedence of security mechanisms](config-xrootd.md#precedence-of-security-mechanisms)
    - [Other configuration options](config-xrootd.md#other-configuration-options)
    - [Third-party transfer](config-xrootd.md#xrootd-third-party-transfer)

1. [dCache as NFSv4.1 Server](config-nfs.md)
    - [Setting up](config-nfs.md#setting-up)
    - [Configuring NFSv4.1 door with GSS-API support](config-nfs.md#configuring-nfsv41-door-with-gss-api-support)
    - [Configuring principal-id mapping for NFS access](config-nfs.md#configuring-principal-id-mapping-for-nfs-access)

1. [dCache Storage Resource Manager](config-SRM.md)
    - [Introduction](config-SRM.md#introduction)
    - [Configuring the srm service](config-SRM.md#configuring-the-srm-service)
    - [The Basic Setup](config-SRM.md#the-basic-setup)
    - [Important srm configuration options](config-SRM.md#important-srm-configuration-options)
    - [Utilization of Space Reservations for Data Storage](config-SRM.md#utilization-of-space-reservations-for-data-storage)
    - [Properties of Space Reservation](config-SRM.md#properties-of-space-reservation)
    - [dCache specific concepts](config-SRM.md#dcache-specific-concepts)
    - [Activating SRM SpaceManager](config-SRM.md#activating-rsm-spacemanager)
    - [Explicit and Implicit Space Reservations for Data Storage in dCache](config-SRM.md#explicit-and-implicit-space-reservations-for-data-storage-in-dcache)
    - [SpaceManager configuration](config-SRM.md#spacemanager-configuration)
    - [SRM SpaceManager and Link Groups](config-SRM.md#srm-spacemanager-and-link-groups)
    - [Making a Space Reservation](config-SRM.md#making-a-space-reservation)
    - [SRM configuration for experts](config-SRM.md#srm-configuration-for-experts)
    - [Configuring the PostgreSQL Database](config-SRM.md#configuring-the-postgresql-database)
    - [SRM or srm monitoring on a separate node](config-SRM.md#srm-or-srm-monitoring-on-a-separate-node)
    - [General SRM Concepts (for developers)](config-SRM.md#general-srm-concepts-for-developers)
    - [The SRM service](config-SRM.md#the-srm-service)
    - [Space Management Functions](config-SRM.md#space-management-functions)
    - [Data Transfer Functions](config-SRM.md#data-transfer-functions)
    - [Request Status Functions](config-SRM.md#request-status-functions)
    - [Directory Functions](config-SRM.md#directory-functions)
    - [Permission functions](config-SRM.md#permission-functions)

1. [The statistics Service](config-statistics.md)
    - [The Basic Setup](config-statistics.md#the-basic-setup)
    - [The Statistics Web Page](config-statistics.md#the-statistics-web-page)
    - [Explanation of the File Format of the xxx.raw Files](config-statistics.md#explanation-of-the-file-format-of-the-xxxraw-files)

1. [The billing Service](config-billing.md)
    - [The billing log files](config-billing.md#the-billing-log-files)
    - [The billing database](config-billing.md#the-billing-database)
    - [Billing Histogram Data](config-billing.md#billing-histogram-data)
    - [Billing Records](config-billing.md#billing-records)
    - [Upgrading a Previous Installation](config-billing.md#upgrading-a-previous-installation)

1. [The alarms Service](config-alarms.md)
    - [The Basic Setup](config-alarms.md#the-basic-setup)
    - [Configure where the alarms service is Running](config-alarms.md#configure-where-the-alarms-service-is-running)
    - [Types of Alarms](config-alarms.md#types-of-alarms)
    - [Alarm Priority](config-alarms.md#alarm-priority)
    - [Working with Alarms: Shell Commands](config-alarms.md#working-with-alarms-shell-commands)
    - [Working with Alarms: Admin Commands](config-alarms.md#working-with-alarms-admin-commands)
    - [Advanced Service Configuration: Enabling Automatic Cleanup](config-alarms.md#advanced-service-configuration-enabling-automatic-cleanup)
    - [Advanced Service Configuration: Enabling Email Alerts](config-alarms.md#advanced-service-configuration-enabling-email-alerts)
    - [Miscellaneous Properties of the alarms Service](config-alarms.md#miscellaneous-properties-of-the-alarms-service)
    - [Alarms SPI](config-alarms.md#alarms-spi--service-provider-interface-)-

1. [dCache Frontend Service](config-frontend.md)

1. [dCache History Service](config-history.md)

1. [ACLs in dCache](config-acl.md)
    - [Introduction](config-acl.md#introduction)
    - [Configuring ACL support](config-acl.md#configuring-acl-support)
    - [Setting and getting ACLs](config-acl.md#setting-and-getting-acls)
    - [Accessing ACL over NFS mount](config-acl.md#accessing-acl-over-nfs-mount)

1. [GLUE Info Provider](config-info-provider.md)
    - [Internal collection of information](config-info-provider.md#internal-collection-of-information)
    - [Configuring the info provider](config-info-provider.md#configuring-the-info-provider)
    - [Testing the info provider](config-info-provider.md#testing-the-info-provider)
    - [Decommissioning the old info provider](config-info-provider.md#decommissioning-the-old-info-provider)
    - [Publishing dCache information](config-info-provider.md#publishing-dcache-information)
    - [Troubleshooting BDII problems](config-info-provider.md#troubleshooting-bdii-problems)
    - [Updating information](config-info-provider.md#updating-information)

1. [Stage Protection](config-stage-protection.md)
    - [Configuration of Stage Protection](config-stage-protection.md#configuration-of-stage-protection)
    - [Definition of the White List](config-stage-protection.md#definition-of-the-white-list)

1. [Using Space Reservations without SRM](config-write-token.md)
    - [The Space Reservation](config-write-token.md#the-space-reservation)
    - [The WriteToken tag](config-write-token.md#the-writetoken-tag)
    - [Copy a File into the WriteToken](config-write-token.md#copy-a-file-into-the-writetoken)
