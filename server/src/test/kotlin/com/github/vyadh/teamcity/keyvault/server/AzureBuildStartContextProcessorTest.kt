package com.github.vyadh.teamcity.keyvault.server

import com.github.vyadh.teamcity.keyvault.common.KeyVaultFeatureSettings
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.buildContextWithParams
import com.github.vyadh.teamcity.keyvault.server.BuildContexts.featureParams
import com.github.vyadh.teamcity.keyvault.server.KotlinMockitoMatchers.any
import jetbrains.buildServer.parameters.ParametersProvider
import jetbrains.buildServer.serverSide.BuildStartContext
import jetbrains.buildServer.serverSide.SProjectFeatureDescriptor
import jetbrains.buildServer.serverSide.oauth.OAuthConstants
import org.junit.jupiter.api.Test
import org.mockito.Mockito
import org.mockito.Mockito.*

internal class AzureBuildStartContextProcessorTest {

  @Test
  fun tokenNotRequestedBuildDoesNotHaveKeyVaultFeature() {
    val context = buildContextWithIrrelevantOAuthFeature()
    val connector = mock(AzureConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector, never()).requestToken(any())
  }

  @Test
  fun tokenNotWhenBuildDoesNotHaveRelevantParameters() {
    val params = mapOf(
          "key" to "some irrelevant %other:secret% message"
    )
    val context = buildContextWithParams(params)
    val connector = mock(AzureConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector, never()).requestToken(any())
  }

  @Test
  fun tokenRequestedWhenBuildFeatureAndParametersExist() {
    val params = mapOf(
          "key" to "some relevant %keyvault:secret% message"
    )
    val context = buildContextWithParams(params)
    val connector = mock(AzureConnector::class.java)
    val processor = AzureBuildStartContextProcessor(connector)

    processor.updateParameters(context)

    verify(connector).requestToken(KeyVaultFeatureSettings.fromMap(featureParams()))
  }

  private fun buildContextWithIrrelevantOAuthFeature(): BuildStartContext {
    val params = mapOf(OAuthConstants.OAUTH_TYPE_PARAM to "irrelevant")

    val descriptor = Mockito.mock(SProjectFeatureDescriptor::class.java)
    Mockito.`when`(descriptor.parameters).thenReturn(params)

    val paramsProvider = Mockito.mock(ParametersProvider::class.java)
    Mockito.`when`(paramsProvider.all).thenReturn(emptyMap())

    return BuildContexts.buildContextWith(descriptor, paramsProvider)
  }

}
