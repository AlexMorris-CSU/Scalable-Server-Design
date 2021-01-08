DIR="$( cd "$( dirname "$0" )" && pwd )"
JAR_PATH="$DIR/conf/:$DIR/build/libs/Morris_Alex_ASG2-1.0-SNAPSHOT.jar"
MACHINE_LIST="$DIR/machine-list"
SCRIPT="java -cp $JAR_PATH cs455.scaling.client.Client <server-host> <registry-port> <message-rate>"
COMMAND='gnome-terminal --geometry=200x40'
for machine in `cat $MACHINE_LIST`
do
 OPTION='--tab -t "'$machine'" -e "ssh -t '$machine' cd '$DIR'; echo '$SCRIPT'; '$SCRIPT'"'
 COMMAND+=" $OPTION"
done
eval $COMMAND &