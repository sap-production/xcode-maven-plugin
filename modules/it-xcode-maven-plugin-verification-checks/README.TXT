This module contains a dummy test which is used during the integration tests (com.sap.prd.mobile.ios.mios.XCodeValidationChecksTest)
+This test cannot be present inside the it-xcode-maven-plugin project.
+The integration tests inside that project runs before the packaging and before the install phase.
+Hence the tests would not be visible when contained inside the it-xcode-maven-plugin project, they are compiled at that time but not packaged and installed.


