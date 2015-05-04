package com.smartbear.lucybot;

import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.rest.RestServiceFactory;
import com.eviware.soapui.impl.wsdl.WsdlProject;
import com.eviware.soapui.plugins.ActionConfiguration;
import com.eviware.soapui.support.action.support.AbstractSoapUIAction;
import com.eviware.soapui.model.iface.Interface;
import com.eviware.soapui.model.support.ModelSupport;
import com.eviware.soapui.support.StringUtils;
import com.eviware.soapui.support.UISupport;
import com.eviware.soapui.impl.rest.RestService;
import com.eviware.soapui.impl.settings.XmlBeansSettingsImpl;
import com.eviware.x.form.XFormDialog;
import com.eviware.x.form.XFormOptionsField;
import com.eviware.x.form.support.XFormRadioGroup;
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.wordnik.swagger.models.Swagger;

import java.awt.Desktop;
import java.net.URI;

@ActionConfiguration(actionGroup = "EnabledWsdlProjectActions")
public class PortalAction extends AbstractSoapUIAction<WsdlProject> {
		private static final String VERSION = Form.class.getName() + Form.VERSION;

	  private XFormDialog dialog;

    public PortalAction() {
        super("PortalAction", "Create LucyBot Portal", "Creates a LucyBot portal");
    }

    @Override
    public void perform(WsdlProject project, Object param) {
			if (project.getInterfaces(RestServiceFactory.REST_TYPE).isEmpty()) {
					UISupport.showErrorMessage("Project is missing REST APIs");
					return;
			}

			// initialize form
			XmlBeansSettingsImpl settings = project.getSettings();
			if (dialog == null) {
					dialog = ADialogBuilder.buildDialog(Form.class);

					dialog.setValue(Form.VERSION, settings.getString(VERSION, "1.0"));
					dialog.setValue(Form.HOST, "api.example.com");

					XFormRadioGroup apis = (XFormRadioGroup) dialog.getFormField(Form.API);
					String[] apiNames = ModelSupport.getNames(project.getInterfaces(RestServiceFactory.REST_TYPE));
					apis.setOptions(apiNames);
					apis.setValue(apiNames[0]);

					XFormOptionsField protocols = (XFormOptionsField) dialog.getFormField(Form.PROTOCOLS);
					String[] protocolOptions = {"http", "https"};
					protocols.setOptions(protocolOptions);
					protocols.setSelectedOptions(protocolOptions);
			}

			while (dialog.show()) {
					try {
							Object[] protocolObjs = ((XFormOptionsField) dialog.getFormField(Form.PROTOCOLS)).getSelectedOptions();
							if (protocolObjs.length == 0) {
									throw new Exception("You must select at least one protocol");
							}

							String[] selectedProtocols = new String[protocolObjs.length];
							for (int i = 0; i < selectedProtocols.length; ++i) {
								selectedProtocols[i] = String.valueOf(protocolObjs[i]);
							}

							String api = dialog.getValue(Form.API);
              RestService service = (RestService) project.getInterfaceByName(api);
							if (service.getEndpoints().length == 0) {
									throw new Exception("Selected API must contain at least one endpoint");
							}

							String host = dialog.getValue(Form.HOST);

							String version = dialog.getValue(Form.VERSION);
							if (StringUtils.isNullOrEmpty(version)) {
									version = "1.0";
							}

							Swagger2Exporter exporter = new Swagger2Exporter(project);
							String id = exporter.exportToLucyBot(service, version, host, selectedProtocols);
			        String message = "Your LucyBot portal is ready! You can see it at ";
							String url = "lucybot.com/trial/" + id;
							message += url + '\n';
							message += "Do you want to go there now?";
							boolean shouldOpen = UISupport.confirm(message, "Open LucyBot Portal");
							if (shouldOpen) {
              	Desktop.getDesktop().browse(new URI("https://" + url));
							}
					} catch (Exception ex) {
							UISupport.showErrorMessage(ex);
					}
			}
    }

		@AForm(name = "Generate LucyBot Portal",
		       description = "Create a LucyBot portal for one of your REST APIs. The portal will be private until you publish it.")
		public interface Form {
				@AField(name = "API",
								description = "Select which REST API to view on LucyBot",
								type = AFieldType.RADIOGROUP)
				public final static String API = "API";

				@AField(name = "Host",
				 				description = "IP address or domain that the API will be hosted on (port optional)",
								type = AFieldType.STRING)
				public final static String HOST = "Host";

				@AField(name = "API Version",
				        description = "API Version",
								type = AFieldType.STRING)
				public final static String VERSION = "API Version";

				@AField(name = "Protocols",
				        description="Select which protocols your API supports",
				        type = AFieldType.MULTILIST)
				public final static String PROTOCOLS = "Protocols";
		}
}
