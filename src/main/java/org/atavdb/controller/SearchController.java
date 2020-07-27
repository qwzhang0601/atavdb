package org.atavdb.controller;

import java.util.ArrayList;
import javax.servlet.http.HttpSession;
import org.atavdb.global.Data;
import org.atavdb.model.SearchFilter;
import org.atavdb.service.model.SampleManager;
import org.atavdb.model.Variant;
import org.atavdb.service.model.VariantManager;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.servlet.ModelAndView;
import org.atavdb.service.util.DBManager;
import org.atavdb.service.util.ErrorManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.web.bind.annotation.PathVariable;

/**
 *
 * @author nick
 */
@Controller
@ComponentScan("org.atavdb.service")
@ComponentScan("org.atavdb.model")
public class SearchController implements ApplicationContextAware {

    private ApplicationContext applicationContext;

    @Autowired
    DBManager dbManager;

    @Autowired
    VariantManager variantManager;

    @Autowired
    SampleManager sampleManager;

    @Autowired
    ErrorManager errorManager;

    @Override
    public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
        this.applicationContext = applicationContext;
    }

    @GetMapping("/search")
    public ModelAndView search(String query, String maxAF, String phenotype,
            String isHighQualityVariant, String isUltraRareVariant,
            String isPublicAvailable, HttpSession session) {
        session.setAttribute("query", query);
        session.setAttribute("phenotype", phenotype);
        session.setAttribute("maxAF", maxAF);
        session.setAttribute("isHighQualityVariant", isHighQualityVariant);
        session.setAttribute("isUltraRareVariant", isUltraRareVariant);
        session.setAttribute("isPublicAvailable", isPublicAvailable);

        SearchFilter filter = applicationContext.getBean(SearchFilter.class);
        filter.init(session);
        if (filter.getQueryType().equals(Data.QUERT_TYPE[1])) {
            return new ModelAndView("redirect:/variant/" + query);
        } else if (filter.getQueryType().equals(Data.QUERT_TYPE[2])) {
            return new ModelAndView("redirect:/gene/" + query);
        } else if (filter.getQueryType().equals(Data.QUERT_TYPE[3])) {
            return new ModelAndView("redirect:/region/" + query);
        } else {
            session.setAttribute("error", filter.getError());
        }

        return new ModelAndView("index");
    }

    @GetMapping("/variant/{variant}")
    public ModelAndView variant(@PathVariable String variant, String phenotype, HttpSession session) {
        session.setAttribute("query", variant);
        if (phenotype != null) {
            session.setAttribute("phenotype", phenotype);
        }
        return doSearch(session);
    }

    @GetMapping("/gene/{gene}")
    public ModelAndView gene(@PathVariable String gene, String phenotype, HttpSession session) {
        session.setAttribute("query", gene);
        if (phenotype != null) {
            session.setAttribute("phenotype", phenotype);
        }
        return doSearch(session);
    }

    @GetMapping("/region/{region}")
    public ModelAndView region(@PathVariable String region, String phenotype, HttpSession session) {
        session.setAttribute("query", region);
        if (phenotype != null) {
            session.setAttribute("phenotype", phenotype);
        }
        return doSearch(session);
    }

    private ModelAndView doSearch(HttpSession session) {
        ModelAndView mv = new ModelAndView("index");

        try {
            dbManager.init();

            SearchFilter filter = applicationContext.getBean(SearchFilter.class);
            filter.init(session);
            sampleManager.init(filter);
            session.setAttribute("sampleCount", sampleManager.getTotalSampleNum(filter));
            session.setAttribute("error", filter.getError());

            if (filter.isQueryValid()) {
                ArrayList<Variant> variantList = variantManager.getVariantList(filter, mv);
                mv.addObject("variantList", variantList);

                if (variantList.isEmpty()) {
                    mv.addObject("message", "No results found from search query.");
                    mv.addObject("flankingRegion", filter.getFlankingRegion());
                }
            }
        } catch (Exception ex) {
//             debug purpose
            mv.addObject("error", errorManager.convertStackTraceToString(ex));
        }

        return mv;
    }
}