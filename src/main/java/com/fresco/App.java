package com.fresco;

import java.io.IOException;
import java.io.Reader;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {

	public static void main(String[] args) throws IOException, URISyntaxException {
		record IndicadorYear(Integer year, Double value) {};
		record IndicadorGeneral(String countryName, String countryCode, String indicatorName, String indicatorCode,
				List<IndicadorYear> listaYear) {};

		List<IndicadorGeneral> listInd = new ArrayList<>();
		CSVParser parser = new CSVParserBuilder()
				           .withSeparator(',')
				           .withIgnoreQuotations(false)
				           .build();
		try (Reader reader = Files.newBufferedReader(
				Paths.get("API_NY.GDP.PCAP.CD_DS2_es_csv_v2_3731817.csv")))
		{
			try (CSVReader csvReader = new CSVReaderBuilder(reader)
				    .withSkipLines(5)
				    .withCSVParser(parser)
				    .build())
			{
				List<String[]> list = new ArrayList<>();
			    list = csvReader.readAll();
			    list.forEach( ind -> {
			    	int year = 1960;
			    	List<IndicadorYear> listIndYear = new ArrayList<>();
			    	for (int i = 4; i < ind.length -1; i++) {
			    		listIndYear.add(new IndicadorYear(year,ind[i].isBlank() ? 0.0 : Double.valueOf(ind[i])));
			    		year++;
			    	}
			    	listInd.add(new IndicadorGeneral(ind[0],ind[1],ind[2],ind[3], listIndYear));
			    });
			}
		}
		//buscar un pais
		var consulta = listInd.stream()
	                          .filter( ind -> ind.countryCode.equals("USA"))
	                          .findFirst();
		if (consulta.isEmpty()) {
			System.out.println("No se encontro el registro");
		}
		else {
			System.out.println(consulta.get().countryName);
			consulta.get().listaYear.forEach( iy -> {
				System.out.println(String.format("\t %d %.4f", iy.year,iy.value));
			});
		}
		//listar los primeros 10 paises con mayor pib del 2020 y mostrar su pib
		record RegistroPais (String countryName, Double pib) {};
		var topMayorPib2020 = listInd.stream()
				               .map(ind -> {
				            	   var pib = ind.listaYear.stream()
				            			                      .filter( iy -> iy.year == 2020)
				            			                      .findFirst();
				            	   return new RegistroPais(ind.countryName,pib.isPresent() ? pib.get().value : 0.0);
				               })
				               .filter(r -> r.pib > 0)
				               .sorted( (a,b) -> b.pib.compareTo(a.pib))
				               .limit(10)
				               .toList();
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("LOS MAS ALTOS 2020");
		topMayorPib2020.forEach( r -> {
			String pib = String.format("%.4f", r.pib);
			pib = " ".repeat(15 - pib.length()) + pib;
			System.out.println(String.format("%s %s %s",r.countryName, " ".repeat(35 - r.countryName.length()), pib));	
		});
		//listar los ultimos 10 paises con menor pib del 2019 y mostrar su pib
		var topMenorPib2019 = listInd.stream()
	               .map(ind -> {
	            	   var pib = ind.listaYear.stream()
	            			                      .filter( iy -> iy.year == 2019)
	            			                      .findFirst();
	            	   return new RegistroPais(ind.countryName,pib.isPresent() ? pib.get().value : 0.0);
	               })
	               .filter(r -> r.pib > 0)
	               .sorted( (a,b) -> a.pib.compareTo(b.pib))
	               .limit(10)
	               .toList();
		System.out.println("- - - - - - - - - - - - - - - - - - - - - - - - - - - - - - - -");
		System.out.println("LOS MAS BAJOS 2019");
		topMenorPib2019.forEach( r -> {
			String pib = String.format("%.4f", r.pib);
			pib = " ".repeat(15 - pib.length()) + pib;
			System.out.println(String.format("%s %s %s",r.countryName, " ".repeat(35 - r.countryName.length()), pib));	
		});
	}
}
