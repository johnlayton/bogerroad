# -*- mode: Python -*-
#
load('ext://tilt_inspector', 'tilt_inspector')
load('ext://configmap', 'configmap_create', 'configmap_from_dict')
load('ext://secret', 'secret_yaml_generic', 'secret_from_dict')
load('ext://helm_remote', 'helm_remote')
load('ext://pack', 'pack')

# tilt_inspector()
analytics_settings(enable=False)


#
# #helm_remote('mysql',
# #            repo_name='stable',
# #            repo_url='https://charts.helm.sh/stable')
#
# # helm_remote('mariadb',
# #             repo_name='bitnami',
# #             repo_url='https://charts.bitnami.com/bitnami',
# #             values='tilt/mariadb/kubernetes-mariadb-values.yaml')
# # k8s_resource('mariadb', port_forwards=['3306:3306'])
#
helm_remote('postgresql',
            repo_name='bitnami',
            repo_url='https://charts.bitnami.com/bitnami',
            values='tilt/postgresql/kubernetes-postgresql-values.yaml')
k8s_resource(workload='postgresql-postgresql', port_forwards=['5432:5432'])
#
# # helm_remote('jaeger',
# #             repo_name='jaegertracing',
# #             repo_url='https://jaegertracing.github.io/helm-charts')
# # k8s_resource(workload='jaeger', port_forwards=['5432:5432'])
#
# k8s_yaml('tilt/zipkin/kubernetes-zipkin.yaml')
# k8s_resource('zipkin', port_forwards=['9411:9411'])
#
# # k8s_yaml('tilt/jaeger/kubernetes-jaeger.yaml')
# # k8s_resource('jaeger', port_forwards=['16686:16686','9411:9411'])
#
# k8s_yaml('tilt/otel/kubernetes-otel.yaml')
# k8s_resource('otel', port_forwards=['4317:4317', '9412:9412'])
#
# # k8s_yaml('tilt/jaeger/kubernetes-jaeger.yaml')
# # k8s_resource('jaeger', port_forwards=['16686:16686','9411:9411'])
# #
# # k8s_yaml('tilt/otel/kubernetes-otel.yaml')
# # k8s_resource('otel', port_forwards=['4317:4317'])
#
# # k8s_yaml('tilt/otel/kubernetes-otel-demo.yaml')
# # k8s_resource('otel', port_forwards=['4317:4317','9411:9411', '16686:16686'])
#
# k8s_yaml('tilt/kafka/kubernetes-zookeeper.yaml')
# k8s_resource('zookeeper', port_forwards=['2181:2181'])
#
# k8s_yaml('tilt/kafka/kubernetes-kafka.yaml')
# k8s_resource('kafka', port_forwards=['9092'])
#
# k8s_yaml('tilt/kafka/kubernetes-kafka-manager.yaml')
# k8s_resource('kafka-manager', port_forwards=['9000', '9999'])
#
# k8s_yaml('tilt/rabbitmq/kubernetes-rabbitmq.yaml')
# k8s_resource('rabbitmq', port_forwards='15672')
#
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

# custom_build('bulk-trace-api',
#     './gradlew --no-daemon bulk-trace:api:bootBuildImage --imageName=$EXPECTED_REF',
#     deps=['bulk-trace/api/src'])
# k8s_yaml('tilt/trace/kubernetes-api-application.yaml')
# k8s_resource('bulk-trace-api', port_forwards=['8081:8080'])

k8s_yaml(secret_from_dict("github", inputs = {
    'GITHUB_TOKEN'              : os.getenv("GITHUB_TOKEN"),
    'AUTH_GITHUB_CLIENT_ID'     : os.getenv("AUTH_GITHUB_CLIENT_ID"),
    'AUTH_GITHUB_CLIENT_SECRET' : os.getenv("AUTH_GITHUB_CLIENT_SECRET")
}))
custom_build('bulk-backstage',
             'cd bulk-backstage && yarn clean && yarn install --frozen-lockfile && yarn tsc && yarn build && yarn build-image --tag $EXPECTED_REF',
             deps=['bulk-backstage/packages/backend/src'])
k8s_yaml('tilt/backstage/kubernetes-backstage-application.yaml')
k8s_resource('bulk-backstage', port_forwards=['7000:7000'])

# def secret_from_dict(name, namespace="", inputs={}):
#     """Returns YAML for a generic secret
#     Args:
#         name: The configmap name.
#         namespace: The namespace.
#         inputs: A dict of keys and values to use. Nesting is not supported
#     Returns:
#         The secret YAML as a blob
#     """
#
#     args = [
#         "kubectl",
#         "create",
#         "secret",
#         "generic",
#         name,
#     ]
#
#     if namespace:
#         args.extend(["-n", namespace])
#
#     if type(inputs) != "dict":
#         fail("Bad argument to secret_from_dict, inputs was not dict typed")
#
#     for k,v in inputs.items():
#         args.extend(["--from-literal", "%s=%s" % (k,v)])
#
#     args.extend(["-o=yaml", "--dry-run=client"])
#
#     # print(args)
#
#     return local(args, quiet=True)
