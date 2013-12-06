/*
 * Copyright (c) 2013 Public domain
 * http://animotron.org/snow
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of
 * this software and associated documentation files (the "Software"), to deal in
 * the Software without restriction, including without limitation the rights to
 * use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of
 * the Software, and to permit persons to whom the Software is furnished to do so,
 * subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package snow.db;

//import org.neo4j.graphdb.Node;
//import org.neo4j.graphdb.Relationship;
//import org.neo4j.graphdb.RelationshipType;
//import org.neo4j.graphdb.Transaction;
//import org.neo4j.graphdb.index.IndexHits;
//import org.neo4j.graphdb.index.ReadableIndex;
//import static mneme.Core.DB;
//import static org.neo4j.graphdb.Direction.OUTGOING;
//import static org.neo4j.graphdb.DynamicRelationshipType.withName;

/**
 * @author <a href="mailto:shabanovd@gmail.com">Dmitriy Shabanov</a>
 * 
 */
public class TermInDB { // implements TermMutable {

//    public static TermInDB term(ID id) {
//        TermInDB term = new TermInDB(id);
//
//        if (term.node == null) throw new IllegalArgumentException("no term with id = '" + id._ + "'.");
//
//        return term;
//    }
//
//    public static TermInDB term(Term term) {
//        if (term instanceof TermInDB) {
//            return (TermInDB) term;
//
//        } else if (term instanceof TermUnstable) {
//            return new TermInDB((TermUnstable) term);
//        }
//
//        throw new IllegalArgumentException("TermInDB can't be created from " + term.getClass() + ".");
//    }
//
//    public static TermInDB term(Node node) {
//        return new TermInDB(node);
//    }
//
//    static RelationshipType R_LABEL = withName(LABEL_LANG.toString());
//
//    Node node;
//
//    private TermInDB(Node node) {
//        this.node = node;
//    }
//
//    private TermInDB(TermUnstable term) {
//
//        try (Transaction txn = DB.beginTx()) {
//
//            // search
//            // ReadableIndex<Node> index =
//            // DB.index().getNodeAutoIndexer().getAutoIndex();
//            //
//            // try ( IndexHits<Node> hits = index.get(IDENTIFIER._, term.id()._)
//            // ) {
//            // for ( Node hit : hits ) {
//            // node = hit;
//            // return;
//            // }
//            // }
//
//            // write one
//            node = DB.createNode();
//            node.setProperty(IDENTIFIER._, ID.random()._);
//            node.setProperty(TYPE._, term.type()._);
//
//            for (Entry<ID, String> entry : term.labels().entrySet()) {
//                label(entry.getKey(), entry.getValue());
//            }
//
//            try {
//                link(new URL(term.link()));
//            } catch (MalformedURLException e) {
//                // can't be ... log?
//            }
//
//            try {
//                visual(new URL(term.visual()));
//            } catch (MalformedURLException e) {
//                // can't be ... log?
//            }
//
//            for (String key : term.propertiesKey()) {
//                property(key, term.property(key));
//            };
//
//            txn.success();
//        }
//
//    }
//
//    private TermInDB(ID id) {
//
//        try (Transaction txn = DB.beginTx()) {
//            // search
//            ReadableIndex<Node> index = DB.index().getNodeAutoIndexer().getAutoIndex();
//
//            try (IndexHits<Node> hits = index.get(IDENTIFIER._, id._)) {
//                for (Node hit : hits) {
//                    node = hit;
//
//                    txn.success();
//
//                    return;
//                }
//            }
//
//            txn.success();
//        }
//    }
//
//    @Override
//    public ID id() {
//        return fromString((String) node.getProperty(IDENTIFIER._));
//    }
//
//    @Override
//    public ID type() {
//        return fromString((String) node.getProperty(TYPE._));
//    }
//
//    @Override
//    public void type(ID type) {
//        node.setProperty(TYPE._, type._);
//    }
//
//    @Override
//    public void label(ID language, String chars) {
//
//        Node label = searchForLabel(language);
//
//        if (label != null)
//            LabelInDB.chars(label, chars);
//        else {
//            node.createRelationshipTo(LabelInDB.label(language, chars), R_LABEL);
//        }
//    }
//
//    @Override
//    public String label(ID language) {
//
//        Node label = searchForLabel(language);
//
//        if (label != null) return LabelInDB.chars(label);
//
//        return null;
//    }
//
//    @Override
//    public String label() {
//
//        Node label = searchForLabel(null);
//
//        if (label != null) return LabelInDB.chars(label);
//
//        return null;
//    }
//
//    private Node searchForLabel(ID language) {
//        for (Relationship rel : node.getRelationships(R_LABEL, OUTGOING)) {
//
//            Node label = rel.getEndNode();
//
//            if (language == null || LabelInDB.isLanguage(language, label)) {
//                return label;
//            }
//        }
//
//        return null;
//    }
//
//    // @Override
//    // public List<Label> labels() {
//    // return null;
//    // }
//
//    @Override
//    public void link(URL url) {
//        node.setProperty(LINK._, url.toString());
//    }
//
//    @Override
//    public String link() {
//        return (String) node.getProperty(LINK._);
//    }
//
//    @Override
//    public void visual(URL url) {
//        node.setProperty(VISUAL._, url.toString());
//    }
//
//    @Override
//    public String visual() {
//        return (String) node.getProperty(VISUAL._);
//    }
//
//    @Override
//    public void property(String key, String value) {
//        node.setProperty(key, value);
//    }
//    
//    public ObjectNode toJson() {
//        
//        ObjectNode obj = JsonNodeFactory.instance.objectNode();
//        
//        try {
//            obj.put("id", id()._);
//        } catch (Throwable e) {}
//        
//        try {
//            obj.put("name", label());
//        } catch (Throwable e) {}
//
//        try {
//            obj.put("visual", visual());
//        } catch (Throwable e) {}
//        
//        
//        return obj;
//    }
//
//    @Override
//    public String debug(int offset) {
//
//        char[] ws = new char[offset + 1];
//        Arrays.fill(ws, ' ');
//        ws[offset] = 0;
//
//        StringBuilder sb = new StringBuilder();
//        sb.append(ws).append("TERM neo4j [");
//
//        ws[offset] = ' ';
//
//        try (Transaction txn = DB.beginTx()) {
//
//            sb.append(Primitives.label(type()._)).append("] ").append(id()).append("\n");
//
//            // sb.append(ws).append(" link '").append(link()).append("'\n")
//            // .append(ws).append(" visual '").append(visual()).append("'\n");
//
//            for (String key : node.getPropertyKeys()) {
//                sb.append(ws).append(Primitives.label(key)).append(" = ").append(Primitives.label((String) node.getProperty(key))).append("\n");
//            }
//
//            sb.append(ws).append("labels:\n");
//            // labels
//            for (Relationship rel : node.getRelationships(R_LABEL, OUTGOING)) {
//
//                LabelInDB label = LabelInDB.label(rel.getEndNode());
//
//                sb.append(ws).append(" ").append(label.debug(0)).append("\n");
//            }
//
//            txn.success();
//        }
//
//        return sb.toString();
//    }
}
