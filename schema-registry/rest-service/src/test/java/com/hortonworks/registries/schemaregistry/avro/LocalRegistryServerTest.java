/**
 * Copyright 2016 Hortonworks.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 **/
package com.hortonworks.registries.schemaregistry.avro;

import com.google.common.io.Resources;
import com.hortonworks.registries.schemaregistry.webservice.LocalSchemaRegistryServer;
import com.hortonworks.registries.schemaregistry.SchemaIdVersion;
import com.hortonworks.registries.schemaregistry.SchemaMetadata;
import com.hortonworks.registries.schemaregistry.SchemaVersion;
import com.hortonworks.registries.schemaregistry.client.SchemaRegistryClient;
import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import java.io.File;
import java.util.Collections;
import java.util.Map;

/**
 *
 */
public class LocalRegistryServerTest {

    @Test
    public void testSanity() throws Exception {
        String configPath = new File(Resources.getResource("schema-registry-test.yaml").toURI()).getAbsolutePath();
        LocalSchemaRegistryServer localSchemaRegistryServer = new LocalSchemaRegistryServer(configPath);
        try {
            localSchemaRegistryServer.start();
            SchemaRegistryClient schemaRegistryClient = createSchemaRegistryClient(localSchemaRegistryServer.getLocalPort());

            // registering schema metadata
            SchemaMetadata schemaMetadata = new SchemaMetadata.Builder("foo").type("avro").build();
            Long schemaId = schemaRegistryClient.registerSchemaMetadata(schemaMetadata);
            Assert.assertNotNull(schemaId);

            // registering a new schema
            String schemaName = schemaMetadata.getName();
            String schema1 = IOUtils.toString(LocalRegistryServerTest.class.getResourceAsStream("/schema-1.avsc"), "UTF-8");
            SchemaIdVersion v1 = schemaRegistryClient.addSchemaVersion(schemaName, new SchemaVersion(schema1, "Initial version of the schema"));
        } finally {
            localSchemaRegistryServer.stop();
        }
    }

    private SchemaRegistryClient createSchemaRegistryClient(int localPort) {
        final String rootUrl = String.format("http://localhost:%d/api/v1", localPort);
        final Map<String, String> SCHEMA_REGISTRY_CLIENT_CONF = Collections.singletonMap(SchemaRegistryClient.Configuration.SCHEMA_REGISTRY_URL.name(), rootUrl);
        return new SchemaRegistryClient(SCHEMA_REGISTRY_CLIENT_CONF);
    }
}
