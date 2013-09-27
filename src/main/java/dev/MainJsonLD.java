/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package dev;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.FileUtils;
import com.hp.hpl.jena.vocabulary.RDF;
import jenajsonld.JsonLDReader;
import jenajsonld.JsonLDWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;

public class MainJsonLD
{
    private static Logger log = LoggerFactory.getLogger("JsonLD") ;
    
    public static void main(String[] args) throws IOException
    {
      JsonLDWriter writer = new JsonLDWriter(true);
      JsonLDReader reader = new JsonLDReader();
        {
            Model m = ModelFactory.createDefaultModel();
            m.read(new FileReader("nested.ttl"), "",FileUtils.langTurtle);
            writer.write(new PrintWriter(System.out), m.getGraph(), "");
            System.exit(0) ;
        }

        {
            Model m = reader.read(new FileInputStream("testing/RIOT/jsonld/graph1.jsonld"), "").getDefaultModel() ;
            m.setNsPrefix("", "http://example/") ;
            m.setNsPrefix("rdf", RDF.getURI()) ;
            m.write(System.out,FileUtils.langTurtle);
            System.exit(0) ;
        }
    }

}

