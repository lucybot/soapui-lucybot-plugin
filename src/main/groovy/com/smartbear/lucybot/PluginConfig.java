package com.smartbear.lucybot;

import com.eviware.soapui.plugins.PluginAdapter;
import com.eviware.soapui.plugins.PluginConfiguration;

/**
 * Created by bbrennan on 04/30/2015.
 */

@PluginConfiguration(
        groupId = "com.smartbear.soapui.plugins",
        name = "LucyBot Plugin",
        version = "0.1",
        autoDetect = true,
        description = "Generates a LucyBot developer portal from Ready! API",
        infoUrl = "https://github.com/lucybot/soapui-lucybot-plugin")
public class PluginConfig extends PluginAdapter {
    @Override
    public void initialize() {
        super.initialize();
    }
}
