# -*- mode: Python -*-

load('ext://helm_remote', 'helm_remote')

#helm_remote('mysql',
#            repo_name='stable',
#            repo_url='https://charts.helm.sh/stable')

# helm_remote('mariadb',
#             repo_name='bitnami',
#             repo_url='https://charts.bitnami.com/bitnami',
#             values='tilt/mariadb/kubernetes-mariadb-values.yaml')
# k8s_resource('mariadb', port_forwards=['3306:3306'])

helm_remote('postgresql',
            repo_name='bitnami',
            repo_url='https://charts.bitnami.com/bitnami',
            values='tilt/postgresql/kubernetes-postgresql-values.yaml')
k8s_resource(workload='postgresql-postgresql', port_forwards=['5432:5432'])

# k8s_yaml('tilt/kafka/kubernetes-zookeeper.yaml')
# k8s_resource('zookeeper', port_forwards=['2181:2181'])
#
# k8s_yaml('tilt/kafka/kubernetes-kafka.yaml')
# k8s_resource('kafka', port_forwards=['9092'])
#
# k8s_yaml('tilt/kafka/kubernetes-kafka-manager.yaml')
# k8s_resource('kafka-manager', port_forwards=['9000', '9999'])

# k8s_yaml('tilt/rabbitmq/kubernetes-rabbitmq.yaml')
# k8s_resource('rabbitmq', port_forwards='15672')

# custom_build('bulk-stream',
#     'cd bulk-stream && ./gradlew --no-daemon bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-stream/src'])
#
# k8s_yaml('tilt/stream/kubernetes-application.yaml')
# k8s_resource('bulk-stream', port_forwards=['6565:6565', '8080:8080'])

# custom_build('bulk-trigger',
#     'cd bulk-trigger && ./gradlew --no-daemon bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-trigger/src'])
#
# k8s_yaml('tilt/trigger/kubernetes-application.yaml')
# k8s_resource('bulk-trigger', port_forwards=['8081:8080'])
