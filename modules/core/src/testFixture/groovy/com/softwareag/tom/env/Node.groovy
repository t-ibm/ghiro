package com.softwareag.tom.env

import java.nio.file.Paths

node {
    name = 'default'
    host {
        ip = '127.0.0.1'
        grpc {
            port = 10997
        }
        info {
            port = 26658
        }
        web3 {
            port = 26660
        }
        tendermint {
            port = 26656
        }
    }
    config {
        location = Paths.get('build/config').toUri().normalize()
    }
    contract {
        registry {
            location = Paths.get('../../modules/contract/build/solidity/test').toUri().normalize()
        }
    }
}

environments {
    zero {
        node {
            name = 'niue'
            host { ip = '10.128.48.26' }
        }
    }
    one {
        node { name = 'ghirouno' }
    }
    two {
        node { name = 'ghirodue' }
    }
    three {
        node { name = 'ghirotre' }
    }
}