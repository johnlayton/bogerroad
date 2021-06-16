# -*- mode: Python -*-

custom_build('bulk-stream',
    'cd bulk-stream && ./gradlew --no-daemon bootBuildImage --imageName=$EXPECTED_REF',
    deps=['bulk-stream/src'])

k8s_yaml('kubernetes-application.yaml')
k8s_resource('bulk-stream', port_forwards=['6565:6565', "8080:8080"])

k8s_yaml('kubernetes-rabbitmq.yaml')
k8s_resource('rabbitmq', port_forwards='15672')
