/*
 * #%L
 * xcode-maven-plugin
 * %%
 * Copyright (C) 2012 SAP AG
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package com.sap.prd.mobile.ios.mios;

import java.util.*;

class CommandLineBuilder {

    private final static String XCODEBUILD = "xcodebuild";
    private final static String TARGET = "target";

    private String configuration;
    private String sdk;
    private XCodeContext xcodeContext;

    public CommandLineBuilder(String configuration, String sdk, XCodeContext ctx) {
        this.configuration = configuration;
        this.sdk = sdk;
        this.xcodeContext = ctx;
    }

    String[] createBuildCall() {
        List<String> result = createBaseCall();
        for (String buildAction : xcodeContext.getBuildActions()) {
            appendValue(result, buildAction);
        }
        return result.toArray(new String[result.size()]);
    }

    String[] createShowBuildSettingsCall() {
        List<String> result = createBaseCall();
        appendKey(result, "showBuildSettings");
        return result.toArray(new String[result.size()]);
    }

    private List<String> createBaseCall() {
        List<String> result = new ArrayList<String>();
        result.add(XCODEBUILD);
        Options.appendOptions(xcodeContext, result, sdk, configuration);
        Settings.appendSettings(xcodeContext, result);
        return result;
    }

    private static void appendKey(List<String> result, String key) {
        check("key", key);
        result.add("-" + key);
    }

    private static void appendValue(List<String> result, String value) {
        check("value", value);
        result.add(value);
    }

    private static void check(final String name, final String forCheck) {
        if (forCheck == null || forCheck.isEmpty())
            throw new IllegalStateException("Invalid " + name + ": " + forCheck + "'. Was null or empty.");
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder(256);
        boolean first = true;
        for (String part : createBuildCall()) {
            if (!first)
                sb.append(" ");
            else
                first = false;
            sb.append(part);
        }
        return sb.toString();
    }

    /**
     * Settings management encapsulated here.
     */
    static class Settings {
        private final static String CODE_SIGN_IDENTITY = "CODE_SIGN_IDENTITY";
        private final static String PROVISIONING_PROFILE = "PROVISIONING_PROFILE";
        private final static String DSTROOT = "DSTROOT";
        private final static String SYMROOT = "SYMROOT";
        private final static String SHARED_PRECOMPS_DIR = "SHARED_PRECOMPS_DIR";
        private final static String OBJROOT = "OBJROOT";
        private final static String XCODE_OUTPUT_DIRECTORY = "build";

        private final static List<String> MANAGED = Arrays.asList(CODE_SIGN_IDENTITY, PROVISIONING_PROFILE, DSTROOT, SYMROOT, SHARED_PRECOMPS_DIR, OBJROOT);
        private final static Map<String, String> REQUIRED = new LinkedHashMap<String, String>(7);

        static {
            // Output directories should be specified (recommended by Apple - http://developer.apple.com/devcenter/download.action?path=/wwdc_2012/wwdc_2012_session_pdfs/session_404__building_from_the_command_line_with_xcode.pdf)
            REQUIRED.put(DSTROOT, XCODE_OUTPUT_DIRECTORY);
            REQUIRED.put(SYMROOT, XCODE_OUTPUT_DIRECTORY);
            REQUIRED.put(SHARED_PRECOMPS_DIR, XCODE_OUTPUT_DIRECTORY);
            REQUIRED.put(OBJROOT, XCODE_OUTPUT_DIRECTORY);
        }

        /**
         * @param userSettings to be validated.
         * @return the passed in userSettings if validation passed without exception
         * @throws IllegalArgumentException if the userSettings contain a key of an XCode setting that is managed by
         *            the plugin.
         */
        public static Map<String, String> validateUserSettings(Map<String, String> userSettings) {
            if (userSettings != null) {
                for (String key : userSettings.keySet()) {
                    if (MANAGED.contains(key))
                        throw new IllegalArgumentException("XCode Setting " + key + " is managed by the plugin and cannot be modified by the user.");
                }
            }
            return userSettings;
        }

        static void appendSettings(XCodeContext xcodeContext, List<String> result) {
            if (xcodeContext.getCodeSignIdentity() != null && !xcodeContext.getCodeSignIdentity().isEmpty()) {
                appendSetting(result, Settings.CODE_SIGN_IDENTITY, xcodeContext.getCodeSignIdentity());
            }

            if (xcodeContext.getProvisioningProfile() != null) {
                appendSetting(result, Settings.PROVISIONING_PROFILE, xcodeContext.getProvisioningProfile());
            }

            Map<String, String> settings = xcodeContext.getSettings();
            if (settings != null) {
                settings.putAll(REQUIRED);
            } else settings = REQUIRED;

            for (Map.Entry<String, String> entry : settings.entrySet()) {
                appendSetting(result, entry.getKey(), entry.getValue());
            }
        }

        private static void appendSetting(List<String> result, String key, String value) {
            result.add(key + "=" + value);
        }
    }

    /**
     * Options management is encapsulated here.
     */
    static class Options {
        private final static String PROJECT_NAME = "project";
        private final static String CONFIGURATION = "configuration";
        private final static String SDK = "sdk";
        private final static List<String> MANAGED = Arrays.asList(PROJECT_NAME, CONFIGURATION, SDK);

        /**
          * @param userOptions to be validated.
          * @return the passed in userOptions if validation passed without exception
          * @throws IllegalArgumentException if the userOptions contain a key of an XCode option that is managed by
          *            the plugin.
          */
         public static Map<String, String> validateUserOptions(Map<String, String> userOptions) {
             if (userOptions != null) {
                 for (String key : userOptions.keySet()) {
                     if (MANAGED.contains(key))
                         throw new IllegalArgumentException("XCode Option '" + key + "' is managed by the plugin and cannot be modified by the user.");
                 }
             }
             return userOptions;
         }

        static void appendOptions(XCodeContext xcodeContext, List<String> result, String sdk, String configuration) {
            Map<String, String> options = xcodeContext.getOptions();
            if (options != null) {
                for (Map.Entry<String, String> entry : options.entrySet()) {
                    appendOption(result, entry.getKey(), entry.getValue());
                }
            }
            appendOption(result, PROJECT_NAME, xcodeContext.getProjectName() + XCodeConstants.XCODE_PROJECT_EXTENTION);
            appendOption(result, CONFIGURATION, configuration);
            if (sdk != null && !sdk.trim().isEmpty()) {
                appendOption(result, SDK, sdk);
            }
            if (xcodeContext.getTarget() != null && !xcodeContext.getTarget().isEmpty()) {
                appendOption(result, TARGET, xcodeContext.getTarget());
            }
        }

        private static void appendOption(List<String> result, String key, String value) {
           check("option", key);
           appendKey(result, key);
           appendValue(result, value);
       }

    }

}
