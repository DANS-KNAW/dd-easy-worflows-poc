/**
 * Copyright (C) 2020 DANS - Data Archiving and Networked Services (info@dans.knaw.nl)
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
 */
package nl.knaw.dans.dd.prepub

import java.net.URI

import better.files.File
import better.files.File.root
import nl.knaw.dans.lib.dataverse.DataverseInstanceConfig
import org.apache.commons.configuration.PropertiesConfiguration

case class Configuration(version: String,
                         serverPort: Int,
                         pidGeneratorBaseUrl: URI,
                         dataverse: DataverseInstanceConfig
                        )

object Configuration {

  def apply(home: File): Configuration = {
    val cfgPath = Seq(
      root / "etc" / "opt" / "dans.knaw.nl" / "dd-pre-publish-workflow",
      home / "cfg")
      .find(_.exists)
      .getOrElse { throw new IllegalStateException("No configuration directory found") }
    val properties = new PropertiesConfiguration() {
      setDelimiterParsingDisabled(true)
      load((cfgPath / "application.properties").toJava)
    }

    new Configuration(
      version = (home / "bin" / "version").contentAsString.stripLineEnd,
      serverPort = properties.getInt("daemon.http.port"),
      pidGeneratorBaseUrl = new URI(properties.getString("pid-generator.base-url") + "/"),
      dataverse = DataverseInstanceConfig(
        baseUrl = new URI(properties.getString("dataverse.base-url")),
        apiToken = properties.getString("dataverse.api-key"),
        connectionTimeout = properties.getInt("dataverse.connection-timeout-ms"),
        readTimeout = properties.getInt("dataverse.read-timeout-ms"),
        apiVersion = properties.getString("dataverse.api-version"),
        awaitLockStateMaxNumberOfRetries = Option(properties.getInt("dataverse.await-lock-max-retries")).getOrElse(10),
        awaitLockStateMillisecondsBetweenRetries = Option(properties.getInt("dataverse.await-lock-wait-time-ms")).getOrElse(1000),
      ))
  }
}

