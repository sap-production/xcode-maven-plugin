//
//  MyAppAppDelegate.h
//  MyApp
//
//  Created by Zahariev, Dobromir on 1/18/11.
//  Copyright __MyCompanyName__ 2011. All rights reserved.
//

#import <UIKit/UIKit.h>

@class MyAppViewController;

@interface MyAppAppDelegate : NSObject <UIApplicationDelegate> {
    UIWindow *window;
    MyAppViewController *viewController;
}

@property (nonatomic, retain) IBOutlet UIWindow *window;
@property (nonatomic, retain) IBOutlet MyAppViewController *viewController;

@end

