package com.softwareag.tom.env
node {
    name = 'default'
    host {
        ip = '127.0.0.1'
        port = 46657
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