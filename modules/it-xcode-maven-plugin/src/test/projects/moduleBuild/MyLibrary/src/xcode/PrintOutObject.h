//
//  PrintOutObject.h
//  MyLibrary
//
//  Created by Zahariev, Dobromir on 1/18/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import <Foundation/Foundation.h>
#import <UIKit/UIKit.h>

@interface PrintOutObject : NSObject {

}

+(void)printout:(NSString*)theValue;
+(void)printout:(NSString*)theValue Label:(UILabel*)theLabel;
@end
