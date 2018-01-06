<table width='100%'>
    <tr>
        <td class='menusection-Settings' colspan='2'>Distributed Application &gt; Contracts</td>
    </tr>
    <tr>
        <td colspan='2'>&nbsp;</td>
    </tr>

    %switch mode%
    %case 'deploy'%
    %invoke wm.dapp.Admin:deployContract%
    %ifvar message%
    <tr><td colspan='2' class='message'>%value message%</td></tr>
    %endif%
    %onerror%
    <tr><td colspan='2' class='message'>error: %value errorMessage%</td></tr>
    %endinvoke%
    %case%
    <tr><td colspan='2' class='nomessage'>&nbsp;</td></tr>
    %endswitch%

    <tr>
        <td><img src='../WmRoot/images/blank.gif' height='10' width='0' border='0'></td>
        <td>
            <table class='tableView' width='100%'>
                <tr><td class='heading' colspan='3'>Solidity Contracts</td></tr>
                <tr class='subheading2'>
                    <td>Contract</td>
                    <td>Address</td>
                    <td>Deployed</td>
                </tr>
                %invoke wm.dapp.Admin:loadContractAddresses%
                %loop contracts%
                <tr>
                    <td class='evenrowdata-l'>%value uri encode(html)%</td>
                    <td class='evenrowdata'>%ifvar address -notempty%%value address encode(html)%%else%-%endif%</td>
                    <td class='evenrowdata'>
                        %ifvar address -notempty%
                        <img src='../WmRoot/images/green_check.png' border='none' width='16' height='16'/>
                        %else%
                        <a class='imagelink' href='contract.dsp?action=deploy&uri=%value uri encode(url)%' onclick="return confirmDeploy('%value uri encode(html)%');">
                            <img src='../WmRoot/icons/checkdot.png' border='none' width='16' height='16'/>
                        </a>
                        %endif%
                    </td>
                </tr>
                %endloop%
                %onerror%
                <tr><td colspan='3' class='message'>error: %value errorMessage%</td></tr>
                %endinvoke%
            </table>
        </td>
    </tr>
</table>