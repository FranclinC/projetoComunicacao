package core;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JLabel;

import protocolo.TCP.CamadaTransporte;
import protocolo.TCP.TCPCliente;
import entidades.Sala;
import entidades.Usuario;
import exceptions.CampoObrigatorioException;
import exceptions.LoginJaExistenteException;
import exceptions.SalaJaExistenteException;
import exceptions.SenhaIncorretaException;
import exceptions.UsuarioInexistenteException;


public class Core {
	ArrayList<Sala> listaSalas;
	ArrayList<Usuario> listaUsuarios;
	ArrayList<Usuario> listaUsuariosOnline;
	CamadaTransporte protocolo;
	Usuario user = new Usuario(null, null, null, null, null);
	Sala sala = new Sala(null, false, null, user);
	boolean existe;
	boolean logado;
	int i;
	FileOutputStream fosU;
	ObjectOutputStream oosU;
	FileOutputStream fosS;
	ObjectOutputStream oosS;
	FileInputStream fisU;
	ObjectInputStream oisU;
	FileInputStream fisS;
	ObjectInputStream oisS;



	public Core(){
		this.listaSalas = new ArrayList<Sala>();

		this.listaUsuarios = new ArrayList<Usuario>();
		this.protocolo = new TCPCliente(4035);
	}


	///////////////////////////////////INICIALIZAR_LISTAS//////////////////////////////////////////////////////////////////////////////////////////
	public void inicializarListas() throws IOException, FileNotFoundException, ClassNotFoundException{


		///Usuarios
		File arqU =new File("arquivosUsuarios.txt");
		fisU = new FileInputStream(arqU);
		oisU = new ObjectInputStream(fisU);
		 
		

      
		while( ( user = (Usuario) oisU.readObject() ) != null){

			listaUsuarios.add(user);  
		}

		oisU.close();

       



		///Salas
		File arqS =new File("arquivosSalas.txt");
		fisS = new FileInputStream("arquivosSalas.txt");
		oisS = new ObjectInputStream(fisS);

		while( ( sala = (Sala) oisS.readObject() ) != null){

			listaSalas.add(sala);  
		}

		oisS.close();

		}

	




	///////////////////////////////////CADASTRAR_USUARIO//////////////////////////////////////////////////////////////////////////////////////////////////////
	public void cadastrarUsuario(String nome,String login, String senha,String email, JLabel avatar) throws LoginJaExistenteException, CampoObrigatorioException, IOException, FileNotFoundException, ClassNotFoundException{
		existe = false;
		i=0;

		while(existe == false && i<listaUsuarios.size()){ // Vejo se j� existe alguem com o mesmo login ja cadastrado
			if (listaUsuarios.get(i).getLogin().equalsIgnoreCase(login)){
				existe=true;
				throw new LoginJaExistenteException(login); // na GUI � que eu a trato
			}else{
				i++;
			}

		}
		
		if(nome.equals("") || login.equals("") || senha.equals("") || email.equals("") || avatar.equals("") ){
			throw new CampoObrigatorioException();
		}else{

		user.setNome(nome);
		user.setLogin(login);
		user.setSenha(senha);
		user.setEmail(email);
		user.setAvatar(avatar);

		

			listaUsuarios.add(user); // adiciono � lista 

			fosU = new FileOutputStream("arquivoUsuarios.txt",true);  // gravar no arquivo e nao sobrescrever
			oosU = new ObjectOutputStream(fosU);

			// salva o objeto
			oosU.writeObject(user);

			oosU.close();
		}
	}





	/////////////////////////////////////////ATUALIZAR_USUARIO////////////////////////////////////////////////////////////////////////////////////////
	public void atualizarUsuario(String nome,String login, String senha,String email, JLabel avatar) throws IOException{
		existe = false;
		logado = false;

		while(existe == false && i<listaUsuarios.size()){ // Vejo se existe alguem com o mesmo login ja cadastrado
			if (listaUsuarios.get(i).getLogin() == login){
				existe=true;
				user = listaUsuarios.get(i);

			}else{
				i++;
			}

		}
		if(existe){
			user.setNome(nome);
			user.setSenha(senha);
			user.setEmail(email);
			user.setAvatar(avatar);

			listaUsuarios.set(i, user); // depois de atualizar o dado na lista e tenho  q atualizar no arquivo!

			fosU = new FileOutputStream("arquivoUsuario.txt",false); // sobreescrever o arquivo com o obj.
			oosU = new ObjectOutputStream(fosU);
			oosU.writeObject(listaUsuarios.get(0)); // pega o primeiro obj. da lista
			oosU.close();
			// salva o objeto



			fosU = new FileOutputStream("arquivoUsuario.txt",true); // escrever depois do arq. ja existente
			oosU = new ObjectOutputStream(fosU);
			for(i=1;i<listaUsuarios.size();i++){
				oosU.writeObject(listaUsuarios.get(i));
			}	

			oosU.close();

		}

	}




	/////////////////////////////////////////LOGIN/////////////////////////////////////////////////////////////////////////////////////////////////
	public void fazerLoginUsuario(String login, String senha) throws UsuarioInexistenteException, SenhaIncorretaException,CampoObrigatorioException{
		existe = false;
		i=0;
		if(login.equals("") || senha.equals("")){
			throw new CampoObrigatorioException();
		}else{
		while(existe == false && i<listaUsuarios.size()){ // Vejo se j� existe alguem com o mesmo login ja cadastrado
			if (listaUsuarios.get(i).getLogin().equals(login)){
				existe=true;
				user = listaUsuarios.get(i);

			}else{
				i++;
			}


		}
		if(existe == true){
			if(senha.equals(user.getSenha())){
				logado = true;
			}else{
				throw new SenhaIncorretaException();
			}
		}else{
			throw new UsuarioInexistenteException(login);
		}

	}
	}






//////////////////////////////////////////MOSTRAR_LISTA_USUARIOS_ONLINE///////////////////////////////////////////////////////////////////////////
	public ArrayList<Usuario> mostrarListaUsuariosOnline() throws IOException, ClassNotFoundException {

		ArrayList<Usuario> retorno = new ArrayList<Usuario>();
		for(int i=0; i<listaUsuarios.size();i++){
			if(listaUsuarios.get(i).getStatus() == true){
				retorno.add(listaUsuarios.get(i));
			}
		}
		
		return retorno;
	}


	
	

	
///////////////////////////////////////////////////////CRIAR_SALA///////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
	public void criarSala(String nome, boolean protegida, String senha, Usuario dono) throws SalaJaExistenteException, CampoObrigatorioException {
		/* toda sala tem que ter a lista de usuarios, e o dono(a pessoa que criou) a sala*/
		boolean existe = false;
		i=0;

		while(existe == false && i<listaSalas.size()){ // Vejo se j� existe alguma sala com o mesmo id ou mesmo nome ja cadastrada
			if (listaSalas.get(i).getNome().equalsIgnoreCase(nome)){
				existe=true;
				throw new SalaJaExistenteException(nome); // na GUI � que eu a trato
			}else{
				i++;
			}

		}
		

		if(nome.equals("") || senha.equals("")){
			throw new CampoObrigatorioException();
		}else{

		sala.setNome(nome);
		sala.setProtegida(protegida);
		sala.setSenha(senha);
		sala.setDono(dono);
		
		sala.getListaUsuarios().add(dono);
		
			listaSalas.add(sala);
		}		
	}


////////////////////////////////////////////////////////ATUALIZAR_SALA///////////////////////////////////////////////////////////////////
	public void atualizarSala(String nome, boolean protegida,String senha) throws IOException{
		
		existe = false;

		while(existe == false && i<listaSalas.size()){ // Vejo se existe alguem com o mesmo login ja cadastrado
			if ((listaSalas.get(i).getNome()).equalsIgnoreCase(nome)){
				existe=true;
				sala= listaSalas.get(i);

			}else{
				i++;
			}

		}
		if(existe){
			sala.setNome(nome);
			sala.setProtegida(protegida);
			sala.setSenha(senha);

			listaSalas.set(i, sala); // depois de atualizar o dado na lista e tenho  q atualizar no arquivo!

			fosS = new FileOutputStream("arquivoSalas.txt",false); // sobreescrever o arquivo com o obj.
			oosS = new ObjectOutputStream(fosS);
			oosS.writeObject(listaSalas.get(0)); // pega o primeiro obj. da lista
			oosS.close();
			// salva o objeto



			fosS = new FileOutputStream("arquivoSalas.txt",true); // escrever depois do arq. ja existente
			oosS = new ObjectOutputStream(fosS);
			for(i=1;i<listaSalas.size();i++){
				oosS.writeObject(listaSalas.get(i));
			}	

			oosS.close();

		}

	}


	
	///////////////////////////////////////////MOSTRAR_LISTA_SALAS///////////////////////////////////////////////////////////////////
	public ArrayList<Sala> mostrarListaSalas(){
		ArrayList<Sala> retorno = new ArrayList<Sala>();
		for(int i=0; i<listaSalas.size();i++){
				retorno.add(listaSalas.get(i));
			}				
		return retorno;
	}
		


	

//////////////////////////////////////////////////////ENVIAR_MSG////////////////////////////////////////////////////////////////////////////////
	public void enviarMensagem(){

	}


}
