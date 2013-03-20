//
//  MyAppAppDelegate.m
//  MyApp
//
//  Created by Zahariev, Dobromir on 1/18/11.
//  Copyright __MyCompanyName__ 2011. All rights reserved.
//

#import "MyAppAppDelegate.h"
#import "MyAppViewController.h"
#import "PrintOutObject.h"

@implementation MyAppAppDelegate

@synthesize window;
@synthesize viewController;


- (BOOL)application:(UIApplication *)application didFinishLaunchingWithOptions:(NSDictionary *)launchOptions {    
    
    // Override point for customization after app launch    
    [window addSubview:viewController.view];
    [window makeKeyAndVisible];
	

	[PrintOutObject printout:@"Some Value is printed ..."];
		
	return YES;
}


- (void)dealloc {
    [viewController release];
    [window release];
    [super dealloc];
}


@end
