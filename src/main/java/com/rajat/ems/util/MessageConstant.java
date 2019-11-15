package com.rajat.ems.util;
import org.springframework.context.MessageSource;
import org.springframework.context.MessageSourceAware;
import org.springframework.stereotype.Service;
import java.util.Locale;

@Service
public class MessageConstant implements MessageSourceAware {

        private MessageSource source;


        public String getMessage(String tag)
        {
            return  this.getMessage(tag,new Object[0]);
        }

//        }
            public String getMessage(String tag,Object... params)
            {
                return  source.getMessage(tag,params, Locale.US);
            }

    @Override
    public void setMessageSource(MessageSource messageSource)
    {
        this.source=messageSource;
    }

    }
