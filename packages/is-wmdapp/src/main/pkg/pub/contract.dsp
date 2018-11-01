<?xml version='1.0'?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
    <title>Distributed Application - Contracts</title>
    <meta http-equiv='content-type' content='text/html; charset=UTF-8'/>
    <link rel='stylesheet' type='text/css' href='../WmRoot/webMethods.css'/>
    <script type='text/javascript' src='../WmRoot/webMethods.js'></script>
    <script type='text/javascript'>
    function confirmDeploy(uri) {
        var msg = "OK to deploy contract '"+uri+"'?\n\nDeploying a contract cannot be undone; if the contract address\nis lost you will not be able to access the contract anymore.\n";
        if (confirm (msg)) {
            document.htmlform_dapp_deploy.uri.value = uri;
            document.htmlform_dapp_deploy.submit();
            return false;
        } else {
            return false;
        }
    }
    function confirmSync(deployedOnly) {
        document.htmlform_dapp_sync.deployedOnly.value = deployedOnly;
        if(deployedOnly) {
            document.htmlform_dapp_sync.submit();
            return false;
        } else {
            var msg = "OK to synchronize all contracts to the IS namespace?\n\nSome contracts might be intended to be imported by other\ntop-level contracts only and might not work by itself.\n\nBe patient; deploying all contracts will take a couple seconds!";
            if (confirm (msg)) {
                document.htmlform_dapp_sync.submit();
                return false;
            } else {
                return false;
            }
        }
    }
    </script>
</head>
<body>
%include contract-table.dsp%
<form name="htmlform_dapp_deploy" action="/WmDApp/contract.dsp" method="post">
    <input type="hidden" name="uri">
    <input type="hidden" name="mode" value="deploy">
</form>
<form name="htmlform_dapp_sync" action="/WmDApp/contract.dsp" method="post">
    <input type="hidden" name="deployedOnly">
    <input type="hidden" name="mode" value="sync">
</form>
<form name="htmlform_dapp_alias" action="/WmDApp/contract.dsp" method="post">
    <input type="hidden" name="mode" value="alias">
</form>
</body>
</html>
