package com.lucybot;

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

/**
 *
 * @author Bobby Brennan
 */

@ActionConfiguration(actionGroup = "EnabledWsdlProjectActions")
public class PortalAction extends AbstractSoapUIAction<WsdlProject> {
	  private static final String LUCYBOT_HOST = "https://lucybot.com";

		private static final String VERSION = ExportForm.class.getName() + ExportForm.VERSION;
		private static final String[] WELCOME_OPTIONS = {
			"See an example",
			"Try an interactive tutorial",
			"Upload my API"
		};
		private XFormDialog exportDialog;
		private XFormDialog welcomeDialog;

    public PortalAction() {
        super("PortalAction", "Create LucyBot Portal", "Creates a LucyBot portal");
				welcomeDialog = ADialogBuilder.buildDialog(WelcomeForm.class);
				XFormRadioGroup actions = (XFormRadioGroup) welcomeDialog.getFormField(WelcomeForm.ACTION);
				actions.setOptions(WELCOME_OPTIONS);
    }

    @Override
    public void perform(WsdlProject project, Object param) {
			while (welcomeDialog.show()) {
				String choice = welcomeDialog.getValue(WelcomeForm.ACTION);
				if (choice.equals(WELCOME_OPTIONS[0])) {
					showExample();
				} else if (choice.equals(WELCOME_OPTIONS[1])) {
					showTutorial();
				} else {
					uploadAPI(project);
				}
			}
		}

		private void showExample() {
			openURL("/portals/hacker_news");
		}

		private void showTutorial() {
      openURL("/demo");
		}

		private void uploadAPI(WsdlProject project) {
			if (project.getInterfaces(RestServiceFactory.REST_TYPE).isEmpty()) {
					UISupport.showErrorMessage("Project is missing REST APIs");
					return;
			}

			// initialize form
			XmlBeansSettingsImpl settings = project.getSettings();
			if (exportDialog == null) {
					exportDialog = ADialogBuilder.buildDialog(ExportForm.class);

					exportDialog.setValue(ExportForm.VERSION, settings.getString(VERSION, "1.0"));
					exportDialog.setValue(ExportForm.HOST, "api.example.com");

					XFormRadioGroup apis = (XFormRadioGroup) exportDialog.getFormField(ExportForm.API);
					String[] apiNames = ModelSupport.getNames(project.getInterfaces(RestServiceFactory.REST_TYPE));
					apis.setOptions(apiNames);
					apis.setValue(apiNames[0]);

					XFormOptionsField protocols = (XFormOptionsField) exportDialog.getFormField(ExportForm.PROTOCOLS);
					String[] protocolOptions = {"http", "https"};
					protocols.setOptions(protocolOptions);
					protocols.setSelectedOptions(protocolOptions);
			}

			while (exportDialog.show()) {
					try {
							Object[] protocolObjs = ((XFormOptionsField) exportDialog.getFormField(ExportForm.PROTOCOLS)).getSelectedOptions();
							if (protocolObjs.length == 0) {
									throw new Exception("You must select at least one protocol");
							}

							String[] selectedProtocols = new String[protocolObjs.length];
							for (int i = 0; i < selectedProtocols.length; ++i) {
								selectedProtocols[i] = String.valueOf(protocolObjs[i]);
							}

							String api = exportDialog.getValue(ExportForm.API);
              RestService service = (RestService) project.getInterfaceByName(api);
							if (service.getEndpoints().length == 0) {
									throw new Exception("Selected API must contain at least one endpoint");
							}

							String host = exportDialog.getValue(ExportForm.HOST);

							String version = exportDialog.getValue(ExportForm.VERSION);
							if (StringUtils.isNullOrEmpty(version)) {
									version = "1.0";
							}

							Swagger2Exporter exporter = new Swagger2Exporter(project);
							String id = exporter.exportToLucyBot(service, version, host, selectedProtocols);
			        String message = "Your LucyBot portal is ready! You can see it at ";
							String url = "/demo_portal/" + id;
							message += "lucybot.com" + url + '\n';
							message += "Do you want to go there now?";
							boolean shouldOpen = UISupport.confirm(message, "Open LucyBot Portal");
							if (shouldOpen) {
              	openURL(url + "?nohelp=true");
							}
					} catch (Exception ex) {
							UISupport.showErrorMessage(ex);
					}
			}
    }

		private void openURL(String url) {
			try {
				URI uri = new URI(LUCYBOT_HOST + url);
				Desktop.getDesktop().browse(uri);
			} catch (Exception ex) {
				UISupport.showErrorMessage(ex);
			}
		}

		@AForm(name = "Welcome to LucyBot!",
		       description = "LucyBot is a web-based tool for documenting and visualizing REST APIs. You can see demos and examples at lucybot.com")
    public interface WelcomeForm {
			@AField(name = "Action",
			        description = "What would you like to do?",
							type = AFieldType.RADIOGROUP)
			public final static String ACTION = "Action";
		}

		@AForm(name = "Generate LucyBot Portal",
		       description = "Create a LucyBot portal for one of your REST APIs. The portal will be private until you publish it.")
		public interface ExportForm {
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
