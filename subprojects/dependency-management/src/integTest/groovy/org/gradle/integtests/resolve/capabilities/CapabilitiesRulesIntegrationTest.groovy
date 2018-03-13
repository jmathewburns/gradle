/*
 * Copyright 2018 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.gradle.integtests.resolve.capabilities

import org.gradle.integtests.resolve.AbstractModuleDependencyResolveTest

class CapabilitiesRulesIntegrationTest extends AbstractModuleDependencyResolveTest {

    def "can declare capabilities using a component metadata rule"() {
        given:
        repository {
            'cglib:cglib:3.2.5'()
            'cglib:cglib-nodep:3.2.5'()
        }

        buildFile << """
            dependencies {
               conf "cglib:cglib-nodep:3.2.5"
               conf "cglib:cglib:3.2.5"
            
               components {
                  withModule('cglib:cglib') {
                     allVariants {
                         withCapabilities {
                             addCapability('cglib', 'cglib', '3.2.5')
                         }
                     }
                  }
                  withModule('cglib:cglib-nodep') {
                     allVariants {
                         withCapabilities {
                             addCapability('cglib', 'cglib', '3.2.5')
                         }
                     }
                  }
               }
            }
        """

        when:
        repositoryInteractions {
            'cglib:cglib-nodep:3.2.5' {
                expectGetMetadata()
            }
            'cglib:cglib:3.2.5' {
                expectGetMetadata()
            }
        }
        fails ':checkDeps'

        then:
        failure.assertHasCause("Cannot choose between cglib:cglib-nodep:3.2.5 and cglib:cglib:3.2.5 because they provide the same capability: cglib:cglib:3.2.5")
    }

    def "can detect conflict with capability in different versions and upgrade automatically to latest version"() {
        given:
        repository {
            'cglib:cglib:3.2.5'()
            'cglib:cglib-nodep:3.2.4'()
        }

        buildFile << """
            dependencies {
               conf "cglib:cglib-nodep:3.2.4"
               conf "cglib:cglib:3.2.5"
            
               components {
                  withModule('cglib:cglib') {
                     allVariants {
                         withCapabilities {
                             addCapability('cglib', 'cglib', '3.2.5')
                         }
                     }
                  }
                  withModule('cglib:cglib-nodep') {
                     allVariants {
                         withCapabilities {
                             addCapability('cglib', 'cglib', '3.2.4')
                         }
                     }
                  }
               }
            }
        """

        when:
        repositoryInteractions {
            'cglib:cglib-nodep:3.2.4' {
                expectGetMetadata()
            }
            'cglib:cglib:3.2.5' {
                expectResolve()
            }
        }
        run ':checkDeps'

        then:
        resolve.expectGraph {
            root(":", ":test:") {
                edge('cglib:cglib-nodep:3.2.4', 'cglib:cglib:3.2.5')
                    .byConflictResolution()
                module('cglib:cglib:3.2.5')
            }
        }
    }

    def "can detect conflict between local project and capability from external dependency"() {
        given:
        repository {
            'org:test:1.0'()
        }

        buildFile << """
            apply plugin: 'java-library'
            
            configurations.api.outgoing {
                capability 'org:capability:1.0'
            }

            dependencies {
                conf 'org:test:1.0'
                
                components {
                   withModule('org:test') {
                      allVariants {
                          withCapabilities {
                              addCapability('org', 'capability', '1.0')
                          }
                      }
                   }
                }
            }
            
            configurations {
                conf.extendsFrom(api)
            }
        """

        when:
        repositoryInteractions {
            'org:test:1.0' {
                expectGetMetadata()
            }
        }
        fails ':checkDeps'

        then:
        failure.assertHasCause("Cannot choose between :test:unspecified and org:test:1.0 because they provide the same capability: org:capability:1.0")
    }

}
