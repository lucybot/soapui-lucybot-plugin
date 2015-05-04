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
import com.eviware.x.form.support.ADialogBuilder;
import com.eviware.x.form.support.AField;
import com.eviware.x.form.support.AField.AFieldType;
import com.eviware.x.form.support.AForm;
import com.wordnik.swagger.models.Swagger;

import java.awt.Desktop;
import java.net.URI;

@ActionConfiguration(actionGroup = "EnabledWsdlProjectActions")
public class PortalAction extends AbstractSoapUIAction<WsdlProject> {
		private static final String BASE_PATH = Form.class.getName() + Form.BASEPATH;
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
					dialog.setValue(Form.BASEPATH, settings.getString(BASE_PATH, ""));
			}

			XFormOptionsField apis = (XFormOptionsField) dialog.getFormField(Form.APIS);
			apis.setOptions(ModelSupport.getNames(project.getInterfaces(RestServiceFactory.REST_TYPE)));

			while (dialog.show()) {
					try {
							Object[] options = ((XFormOptionsField) dialog.getFormField(Form.APIS)).getSelectedOptions();
							if (options.length == 0) {
									throw new Exception("You must select at least one REST API ");
							}

							RestService[] services = new RestService[options.length];
							for (int c = 0; c < options.length; c++) {
									services[c] = (RestService) project.getInterfaceByName(String.valueOf(options[c]));
									if (services[c].getEndpoints().length == 0) {
											throw new Exception("Selected APIs must contain at least one endpoint");
									}
							}

							// double-check
							if (services.length == 0) {
									throw new Exception("You must select at least one REST API to export");
							}

							String version = dialog.getValue(Form.VERSION);
							if (StringUtils.isNullOrEmpty(version)) {
									version = "1.0";
							}

							Swagger2Exporter exporter = new Swagger2Exporter(project);
							String id = exporter.exportToLucyBot(version,
												"json", services, dialog.getValue(Form.BASEPATH));
			        String message = "Hurray! Your LucyBot portal is ready! You can see it at ";
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

		@AForm(name = "Generate LucyBot Portal", description = "Creates a LucyBot portal for selected REST APIs in this project")
		public interface Form {
				@AField(name = "APIs", description = "Select which REST APIs to include in the Swagger definition", type = AFieldType.MULTILIST)
				public final static String APIS = "APIs";

				@AField(name = "API Version", description = "API Version", type = AFieldType.STRING)
				public final static String VERSION = "API Version";

				@AField(name = "Base Path", description = "Base Path that the Swagger definition will be hosted on", type = AFieldType.STRING)
				public final static String BASEPATH = "Base Path";
		}
}
