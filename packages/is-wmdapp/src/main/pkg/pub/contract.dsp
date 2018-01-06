<?xml version='1.0'?>
<!DOCTYPE HTML PUBLIC "-//W3C//DTD HTML 4.0 Transitional//EN">

<html>
<head>
    <title>Distributed Application - Contracts</title>
    <meta http-equiv='Pragma' content='no-cache'>
    <meta http-equiv='Content-Type' content='text/html; charset=UTF-8'>
    <meta http-equiv='Expires' content='-1'>
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
    </script>
</head>
<body>
%include contract-table.dsp%
<form name="htmlform_dapp_deploy" action="/WmDApp/contract.dsp" method="POST">
    <input type="hidden" name="uri">
    <input type="hidden" name="mode" value="deploy">
</form>
</body>
</html>
