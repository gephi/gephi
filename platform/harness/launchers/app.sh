#!/bin/sh

#
# resolve symlinks
#

PRG=$0

while [ -h "$PRG" ]; do
    ls=`ls -ld "$PRG"`
    link=`expr "$ls" : '^.*-> \(.*\)$' 2>/dev/null`
    if expr "$link" : '^/' 2> /dev/null >/dev/null; then
	PRG="$link"
    else
	PRG="`dirname "$PRG"`/$link"
    fi
done

progdir=`dirname "$PRG"`
APPNAME=`basename "$PRG"`

if [ -f "$progdir/../etc/$APPNAME".conf ] ; then
    . "$progdir/../etc/$APPNAME".conf
fi

# XXX does not correctly deal with spaces in non-userdir params
args=""

case "`uname`" in
    Darwin*)
        userdir="${default_mac_userdir}"
        ;;
    *)
        userdir="${default_userdir}"
        ;;
esac
while [ $# -gt 0 ] ; do
    case "$1" in
        --userdir) shift; if [ $# -gt 0 ] ; then userdir="$1"; fi
            ;;
        *) args="$args \"$1\""
            ;;
    esac
    shift
done

if [ -f "${userdir}/etc/$APPNAME".conf ] ; then
    . "${userdir}/etc/$APPNAME".conf
fi

if [ -n "$jdkhome" -a \! -d "$jdkhome" -a -d "$progdir/../$jdkhome" ]; then
    # #74333: permit jdkhome to be defined as relative to app dir
    jdkhome="$progdir/../$jdkhome"
fi

readClusters() {
  if [ -x /usr/ucb/echo ]; then
    echo=/usr/ucb/echo
  else
    echo=echo
  fi
  while read X; do
    if [ "$X" \!= "" ]; then
      $echo "$progdir/../$X"
    fi
  done
}

clusters=`(cat "$progdir/../etc/$APPNAME".clusters; echo) | readClusters | tr '\012' ':'`

if [ ! -z "$extra_clusters" ] ; then
    clusters="$clusters:$extra_clusters"
fi

nbexec=`echo "$progdir"/../platform*/lib/nbexec`

case "`uname`" in
    Darwin*)
        eval exec sh '"$nbexec"' \
            --jdkhome '"$jdkhome"' \
            -J-Dcom.apple.mrj.application.apple.menu.about.name='"$APPNAME"' \
            -J-Xdock:name='"$APPNAME"' \
            '"-J-Xdock:icon=$progdir/../../$APPNAME.icns"' \
            --clusters '"$clusters"' \
            --userdir '"${userdir}"' \
            ${default_options} \
            "$args"
        ;;
    *)  
       sh=sh
       # #73162: Ubuntu uses the ancient Bourne shell, which does not implement trap well.
       if [ -x /bin/bash ]
       then
           sh=/bin/bash
       fi
       eval exec $sh '"$nbexec"' \
            --jdkhome '"$jdkhome"' \
            --clusters '"$clusters"' \
            --userdir '"${userdir}"' \
            ${default_options} \
            "$args"
       exit 1
        ;;
esac
