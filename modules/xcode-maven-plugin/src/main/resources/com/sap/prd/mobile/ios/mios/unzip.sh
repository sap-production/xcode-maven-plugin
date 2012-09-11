#!/bin/sh

###
# #%L
# xcode-maven-plugin
# %%
# Copyright (C) 2012 SAP AG
# %%
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
# 
#      http://www.apache.org/licenses/LICENSE-2.0
# 
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
# #L%
###

die() {
    echo "$*" >&2
    exit 1
}

ZIPFILE=$1
DESTFOLDER=$2

echo Unzipping $ZIPFILE into $DESTFOLDER

if [ -d "$DESTFOLDER" ]; then
  rm -Rf "$DESTFOLDER"
fi

mkdir -p "$DESTFOLDER" || die "Could not create $DESTFOLDER"
cd "$DESTFOLDER"
unzip "$ZIPFILE" -d "$DESTFOLDER" || die "Could not unzip $ZIPFILE into $DESTFOLDER"
