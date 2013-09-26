package fr.becpg.repo.search.lucene.analysis;

import java.io.IOException;
import java.io.Reader;

import org.apache.lucene.analysis.Token;
import org.apache.lucene.analysis.Tokenizer;

public class NodeRefTokenFilter extends Tokenizer{

	boolean readInput = true;

    boolean lowerCase;
    
    NodeRefTokenFilter(Reader in, boolean lowerCase)
    {
        super(in);
        this.lowerCase = lowerCase;
    }

	@Override
    public Token next() throws IOException
    {
        if (readInput)
        {
            readInput = false;            
            StringBuilder buffer = new StringBuilder();
            int current;
            char c;
            while ((current = input.read()) != -1)
            {
                c = (char) current;
                buffer.append(c);
            }

            String token = buffer.toString();
            if(lowerCase)
            {
                token = token.toLowerCase();
            }
            return new Token(token, 0, token.length(), "ALPHANUM");
        }
        else
        {
            return null;
        }
    }
}
