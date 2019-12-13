/*
 * Copyright (c) 1996-2006 webMethods, Inc.
 * Copyright (c) 2007-2019 Software AG, Darmstadt, Germany and/or Software AG USA Inc., Reston, VA,
 * USA, and/or its subsidiaries and/or its affiliates and/or their licensors.
 * Use, reproduction, transfer, publication or disclosure is prohibited except as specifically
 * provided for in your License Agreement with Software AG.
 */
package wm.dapp

import com.softwareag.tom.is.pkg.dapp.ServiceSupplierWeb3
import com.softwareag.tom.is.pkg.dapp.Util
import com.wm.app.b2b.server.ServiceException
import com.wm.app.b2b.server.dispatcher.wmmessaging.ConnectionAlias
import com.wm.data.IData
import com.wm.data.IDataFactory
import spock.lang.Shared
import spock.lang.Specification

/**
 * System under specification: {@link Admin}.
 * @author tglaeser
 */
class AdminSpecification extends Specification {

    @Shared Admin admin = new Admin()

    def 'test positive create connection alias'() {
        given: 'an input pipeline'
        IData pipeline = IDataFactory.create()

        and: 'the underlying implementation methods are mocked'
        Admin.util = Mock(Util)
        admin.util.getConnectionAlias() >> { result }

        when: 'the service is invoked'
        admin.createConnectionAlias(pipeline)

        then: 'the pipeline is altered as expected'
        pipeline == expected
        notThrown ServiceException

        where: 'the execution result and output pipeline is'
        result << [
            Mock(ConnectionAlias),
            null,
        ]
        expected << [
            IDataFactory.create((Object[][])[['message', 'DApp connection alias already exists.']]),
            IDataFactory.create((Object[][])[['message', 'Successfully created DApp connection alias.']]),
        ]
    }

    def 'test negative create connection alias'() {
        given: 'a test fixture'
        IData pipeline = IDataFactory.create()
        IData expected = IDataFactory.create((Object[][])[['message', 'Failed to create DApp connection alias!']])

        and: 'the underlying implementation methods are mocked'
        Admin.util = Mock(Util)
        admin.util.getConnectionAlias() >> { null }
        admin.util.createConnectionAlias() >> { throw new Exception() }

        when: 'the service is invoked'
        admin.createConnectionAlias(pipeline)

        then: 'the pipeline gets altered as expected'
        pipeline == expected
        thrown ServiceException
    }

    def 'test positive sync contracts'() {
        given: 'a test fixture'
        IData pipeline = IDataFactory.create((Object[][])[['deployedOnly', 'true']])
        IData expected = IDataFactory.create((Object[][])[['deployedOnly', 'true'], ['message', 'Successfully synchronized all contracts to the IS namespace.']])

        and: 'the underlying implementation methods are mocked'
        Admin.util = Mock(Util)
        admin.util.getFunctions(true) >> { [:] }
        admin.util.getEvents(true) >> { [:] }
        admin.util.getTriggers([:]) >> { [] }

        when: 'the service is invoked'
        admin.syncContracts(pipeline)

        then: 'the pipeline gets altered as expected'
        pipeline == expected
        notThrown ServiceException
    }

    def 'test negative sync contracts'() {
        given: 'a test fixture'
        IData pipeline = IDataFactory.create((Object[][])[['deployedOnly', 'true']])
        IData expected = IDataFactory.create((Object[][])[['deployedOnly', 'true'], ['message', 'Failed to synchronize contracts!']])

        when: 'the service is invoked'
        admin.syncContracts(pipeline)

        then: 'the pipeline gets altered as expected'
        pipeline == expected
        thrown ServiceException
    }

    def 'test positive load contract addresses'() {
        given: 'a test fixture'
        IData[] contracts = [IDataFactory.create((Object[][])[['uri', 'sample/util/Console']]), IDataFactory.create((Object[][])[['uri', 'sample/SimpleStorage']])]
        IData pipeline = IDataFactory.create()
        IData expected = IDataFactory.create((Object[][])[['contracts', contracts ]])

        and: 'the underlying implementation methods are mocked'
        Admin.util = Mock(Util)
        admin.util.getContractAddresses() >> { contracts }

        when: 'the service is invoked'
        admin.loadContractAddresses(pipeline)

        then: 'the pipeline gets altered as expected'
        pipeline == expected
        notThrown ServiceException
    }

    def 'test positive deploy contract'() {
        given: 'a test fixture'
        String uri = 'sample/util/Console'
        String address = '0x33F71BB66F8994DD099C0E360007D4DEAE11BFFE'
        IData pipeline = IDataFactory.create((Object[][])[['uri', uri ]])
        IData expected = IDataFactory.create((Object[][])[['uri', uri ], ['message', "Successfully deployed contract '$uri'." as String]])

        and: 'the underlying implementation methods are mocked'
        Admin.util = Mock(Util)
        Admin.util.web3() >> { Mock(ServiceSupplierWeb3) }
        admin.util.web3().deployContract(uri) >> { address }
        admin.util.storeContractAddress(uri, address) >> { }

        when: 'the service is invoked'
        admin.deployContract(pipeline)

        then: 'the pipeline gets altered as expected'
        pipeline == expected
        notThrown ServiceException
    }
}