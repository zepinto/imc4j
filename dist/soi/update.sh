#!/bin/sh

cd ../..
ant soi
cd -
for ip in "10.0.10.12" "10.0.10.13" "10.0.10.14" "10.0.10.15" "10.0.10.16"	
do
	echo "copying to ${ip}3..."
	sed -e "s,host_addr=10.0.10.120,host_addr=${ip}0,g" soi.props > soi_tmp.props
	sshpass -p root ssh -t root@${ip}3 "mkdir -p /opt/lsts/soi; mount / -o remount,rw"
	sshpass -p root scp soi.sh root@${ip}3:/etc/rc.d/soi
	sshpass -p root scp SoiExecutive.jar soi.sh root@${ip}3:/opt/lsts/soi
	sshpass -p root scp soi_tmp.props root@${ip}3:/opt/lsts/soi/soi.props
	sshpass -p root ssh -t root@${ip}3 "mount / -o remount,ro"
done
