#!/bin/bash

# software name
PACKAGE_NAME="workshiftly";

#installation directory
INSTALLATION_DIR=/opt/workshiftly;

# desktop entry name
DESKTOP_ENTRY=~/Desktop/workshiftly.desktop;

# desktop entry generated while dep installation
GENERATED_DESKTOP_ENTRY=workshiftly-WorkShiftly.desktop;

# Remove desktop entry if it is already existing
if [[ -f "$DESKTOP_ENTRY" ]] 
then
	rm $DESKTOP_ENTRY;
	echo "removed previouly created desktop entry $DESKTOP_ENTRY";
fi

# copy generated desktop entry to logged user's desktop
cp -- "$INSTALLATION_DIR/lib/$GENERATED_DESKTOP_ENTRY" "$DESKTOP_ENTRY";

# make copied desktop entry as excutable for user, group and other users
chmod ugo+x $DESKTOP_ENTRY;

# current logged user
CURRENT_USERNAME=$(whoami);
echo "current logged user is $CURRENT_USERNAME";

# change ownership of desktop entry to logged user
chown $CURRENT_USERNAME $DESKTOP_ENTRY;
 
gio set $DESKTOP_ENTRY "metadata::trusted" true;
