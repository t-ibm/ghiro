package com.softwareag.tom.env
node {
    name = 'default'
    host {
        ip = '0.0.0.0'
        port = 1337
        tendermint {
            port = 36657
        }
    }
}

environments {
    one {
        node {
            name = 'ghirouno'
        }
    }
    two {
        node {
            name = 'ghirodue'
        }
    }
    three {
        node {
            name = 'ghirotre'
        }
    }
}