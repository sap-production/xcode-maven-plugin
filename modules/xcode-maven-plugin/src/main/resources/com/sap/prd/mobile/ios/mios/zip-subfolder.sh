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

ROOT_DIR=$1
SUBFOLDER=$2
ZIP_FILENAME=$3
ARCHIVE_FOLDER=$4

echo Zipping the subfolder "$SUBFOLDER" of the dir "$ROOT_DIR" into the file "$ZIP_FILENAME"

cd "$ROOT_DIR" || die "Cannot cd into directory $ROOT_DIR containing the application"

rm -f "$ZIP_FILENAME"

if [ -z "$ARCHIVE_FOLDER" ]; then
  rm -f "$ZIP_FILENAME"
  zip -y -r "$ZIP_FILENAME" "$SUBFOLDER" || die "Error while creating the zip file"
else 
  # if ARCHIVE_FOLDER is set, we first have to copy the sources to an intermediate subfolder
  rm -rf "tmp/$ARCHIVE_FOLDER"
  mkdir -p "tmp/$ARCHIVE_FOLDER" || die "Cannot create tmp/$ARCHIVE_FOLDER directory"
  cp -Rp "$SUBFOLDER" "tmp/$ARCHIVE_FOLDER" || die "Error copying the folder $SUBFOLDER"
  cd tmp
  zip -y -r "$ZIP_FILENAME" "$ARCHIVE_FOLDER" || die "Error while creating the zip file"
  mv -f "$ZIP_FILENAME" ..
fi 
  
