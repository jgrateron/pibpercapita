package com.fresco;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static java.util.Comparator.comparing;
import com.opencsv.CSVParser;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

public class App {

	public record IndYear(Integer year, Double value) {};
	public record IndGen(String cName, String cCode, String indName, String indCode, List<IndYear> listYear) {};
	public record RegistroPais (String countryName, Double pib) {};
	
	public static void main(String[] args) throws IOException 
	{
		var listInd = cargarCsv();
		//buscar un pais
		buscarPais(listInd,"VEN");
		//buscar el nombre de mayor longitud
		var maxName = maxName(listInd);
		//listar los primeros 10 paises con mayor pib del 2020 y mostrar su pib
		losMasAltos(listInd, maxName, 2020);
		//listar los ultimos 10 paises con menor pib del 2019 y mostrar su pib
		losMasBajos(listInd, maxName, 2019);
		//Promedio del PIB de los ultimos 10 años ordenado por nombre del pais
		promUlt10Year(listInd, maxName);
	}
	/*
	 * 
	 */
	private static List<IndGen> cargarCsv() throws IOException
	{
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
			    return csvReader.readAll().stream().map( ind -> {
			    	var year = new AtomicInteger(1960);
			    	var listIndYear = Arrays.asList(ind)
			    	      .stream()
			    	      .limit(ind.length - 1)
			    	      .skip(4)
			    	      .map(val -> new IndYear(year.getAndIncrement(),val.isBlank() ? 0.0 : Double.valueOf(val)))
			    	      .toList();
			    	return new IndGen(ind[0],ind[1],ind[2],ind[3], listIndYear);
			    })
			    .toList();
			}
		}
	}
	/*
	 * 
	 */
	public static int maxName(List<IndGen> listInd) {
		return listInd.stream()
				      .mapToInt(i -> i.cName.length())
				      .max()
				      .orElse(100);
	}
	/*
	 * 
	 */
	private static void promUlt10Year(List<IndGen> listInd, int maxName) {
		var promedioUlt10Year = listInd.stream()
				                       .map(ind -> {
				                    	   var promedio = ind.listYear.stream()
				                    			                       .filter(iy -> iy.year >= 2010)
				                    			                       .mapToDouble( iy -> iy.value)
				                    			                       .average()
				                    			                       .orElse(0);
				                    	   return new RegistroPais(ind.cName, promedio);
				                       })
				                       .sorted(comparing(RegistroPais::countryName) ) //(a,b)-> a.countryName.compareToIgnoreCase(b.countryName)
				                       .toList();
		promedioUlt10Year.forEach( r -> {
			var pib = "%.4f".formatted(r.pib);
			pib = " ".repeat(15 - pib.length()) + pib;
			System.out.println(String.format("%s %s %s",r.countryName, " ".repeat(maxName - r.countryName.length()), pib));
		});
	}
	/*
	 * 
	 */
	private static void losMasBajos(List<IndGen> listInd, int maxName, int year) {
		var topMenorPib2019 = listInd.stream()
	               .map(ind -> {
	            	   var pib = ind.listYear.stream()
	            			                      .filter( iy -> iy.year == year)
	            			                      .findFirst();
	            	   return new RegistroPais(ind.cName, pib.isPresent() ? pib.get().value : 0.0);
	               })
	               .filter(r -> r.pib > 0)
	               .sorted( (a,b) -> a.pib.compareTo(b.pib))
	               .limit(20)
	               .toList();
		System.out.println("- ".repeat(41));
		System.out.println("LOS MAS BAJOS " + year);
		topMenorPib2019.forEach( r -> {
			var pib = "%.4f".formatted(r.pib);
			pib = " ".repeat(15 - pib.length()) + pib;
			System.out.println(String.format("%s %s %s",r.countryName, " ".repeat(maxName - r.countryName.length()), pib));	
		});
		System.out.println("- ".repeat(41));
	}
	/*
	 * 
	 */
	private static void losMasAltos(List<IndGen> listInd, int maxName, int year) {
		
		var topMayorPib2020 = listInd.stream()
				               .map(ind -> {
				            	   var pib = ind.listYear.stream()
				            			                  .filter( iy -> iy.year == year)
				            			                  .findFirst();
				            	   return new RegistroPais(ind.cName, pib.isPresent() ? pib.get().value : 0.0);
				               })
				               .filter(r -> r.pib > 0)
				               .sorted(comparing(RegistroPais::pib).reversed())
				               .limit(20)
				               .toList();
		System.out.println("- ".repeat(41));
		System.out.println("LOS MAS ALTOS " + year);
		topMayorPib2020.forEach( r -> {
			var pib = "%.4f".formatted(r.pib);
			pib = " ".repeat(15 - pib.length()) + pib;
			System.out.println(String.format("%s %s %s",r.countryName, " ".repeat(maxName - r.countryName.length()), pib));	
		});
	}
	/*
	 * 
	 */
	private static void buscarPais(List<IndGen> listInd, String codPais) {
		var consulta = listInd.stream()
	                          .filter( ind -> ind.cCode.equals(codPais))
	                          .findFirst();
		if (consulta.isEmpty()) {
			System.out.println("No se encontro el registro");
		}
		else {
			System.out.println(consulta.get().cName);
			consulta.get().listYear.forEach( iy -> {
				var pib = "%.4f".formatted(iy.value);
				pib = " ".repeat(15 - pib.length()) + pib;
				System.out.println(String.format("\t %d %s", iy.year,pib));
			});
		}
	}
}
