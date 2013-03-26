//
//  PrintOutObject.m
//  MyLibrary
//
//  Created by Zahariev, Dobromir on 1/18/11.
//  Copyright 2011 __MyCompanyName__. All rights reserved.
//

#import "PrintOutObject.h"


@implementation PrintOutObject

+(void)printout:(NSString*)theValue{
	NSLog(@"%@", theValue);
}

+(void)printout:(NSString*)theValue Label:(UILabel*)theLabel{
	[theLabel setText:theValue];
	NSLog(@"In Label is shown:%@", theValue);
}
@end
