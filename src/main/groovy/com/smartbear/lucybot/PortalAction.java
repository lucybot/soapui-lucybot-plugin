import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.impl.rest.RestService;

@ActionConfiguration(actionGroup = "EnabledWsdlProjectActions",
		     targetType = Interface.class)
public class PortalAction extends AbstractSoapUIAction<WsdlProject> {

    public PortalAction() {
        super("PortalAction", "Create LucyBot Portal", "Creates a LucyBot portal");
    }

    @Override
    public void perform(WsdlProject target, Object param) {
			  System.out.println("PERFOM");
        UISupport.prompt("The name of this interface is " + target.getName(), "hi");
    }
}
