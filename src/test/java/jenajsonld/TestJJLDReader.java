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

package jenajsonld;

import static jenajsonld.JenaJSONLD.JSONLD ;

import java.io.ByteArrayInputStream ;
import java.io.ByteArrayOutputStream ;

import org.apache.jena.riot.RDFDataMgr ;
import org.junit.BeforeClass ;
import org.junit.Test ;

import com.hp.hpl.jena.rdf.model.Model ;
import com.hp.hpl.jena.rdf.model.ModelFactory ;

public class TestJJLDReader
{
    // Also includes round-trip tests.
    
    @BeforeClass static public void setupClass() { JenaJSONLD.init(); }  

    
    @Test public void read_01() { test("D1.jsonld", "D1.ttl") ; }
    
    
    
    private void test(String inFile, String outFile)
    {}

    @Test public void roundtrip_01() { rtRJR("D1.ttl") ; }
    
    static void rtRJR(String filename)
    {
        // Read in
        Model model = RDFDataMgr.loadModel(filename) ;
        
        // Write a JSON-LD
        ByteArrayOutputStream out = new ByteArrayOutputStream() ;
        RDFDataMgr.write(out, model, JSONLD) ;
        ByteArrayInputStream r = new ByteArrayInputStream(out.toByteArray()) ;
        
        // Read as JSON-LD 
        Model model2 = ModelFactory.createDefaultModel() ;
        RDFDataMgr.read(model2, r, null, JSONLD) ;
        
        // Compare
        if ( ! model.isIsomorphicWith(model2) ) 
            System.out.println("## ---- DIFFERENT") ;
    }
}


