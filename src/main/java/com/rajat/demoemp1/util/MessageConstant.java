package com.rajat.demoemp1.util;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class MessageConstant implements MessageSourceAware {

        private MessageSource source;

        @Override
        public void setMessageSource(MessageSource messageSource)
        {
            this.source=messageSource;
        }
        public String getMessage(String tag)
        {
            return  this.getMessage(tag,null);
        }

//        }
            public String getMessage(String tag,Object params[])
            {
                return  source.getMessage(tag,params, Locale.US);
            }

    }
