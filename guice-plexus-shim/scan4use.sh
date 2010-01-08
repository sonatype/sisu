#!/bin/sh

if [ $# -lt 2 ]
then
  echo "$0 packagename [ directory | jarfiles... ]"
  exit
fi

PKG=$1
shift

if [ -d $1 ]
then
  JARS=`find $1 -name "*.jar"`
else
  JARS=$@
fi

SEG="([./$][a-z0-9_][a-z0-9_]*)"
CLZ="([./$][A-Z][A-Za-z0-9_]*)"
MTH="([.][a-z][A-Za-z0-9_]*)"

function scan
{
  awk "/$PKG/ {
    while ( match( \$0, \"$PKG$SEG*$CLZ*$MTH?\" ) > 0 )
    {
      USE=substr( \$0, RSTART, RLENGTH );
      gsub( \"[/$]\", \".\", USE );
      print( USE );
      \$0 = substr( \$0, RSTART + RLENGTH );
    }
  }"
}

SCRATCH=`mktemp --tmpdir -d "scan4use.XXXXXXXXXX"`
LISTING=$SCRATCH.lst

for f in $JARS
do
  echo "@ $f"
  rm -fr $SCRATCH
  rm -f $LISTING
  unzip -q -d $SCRATCH $f
  for g in `find $SCRATCH -type f`
  do
    CLASSFILE=${g%.class}
    if [ "$CLASSFILE" != "$g" ]
    then
      ( cd $SCRATCH ; javap -v ${CLASSFILE#$SCRATCH} ) | scan >> $LISTING
    else
      strings $g | scan >> $LISTING
    fi
  done
  sort -u $LISTING
done

rm -fr $SCRATCH
rm -f $LISTING

