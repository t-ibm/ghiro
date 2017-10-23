package com.softwareag.tom.env
node {
    name = 'default'
    host {
        ip = '127.0.0.1'
        port = 1337
        tendermint {
            port = 36657
        }
    }
    contract {
        registry {
            location = '../../modules/abi/build/solc'
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