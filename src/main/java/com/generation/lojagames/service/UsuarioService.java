package com.generation.lojagames.service;

import java.nio.charset.Charset;
import java.util.Optional;

import org.apache.commons.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.generation.lojagames.model.UsuarioLogin;
import com.generation.lojagames.model.UsuarioModel;
import com.generation.lojagames.repository.UsuarioRepository;

@Service
public class UsuarioService {
	
	@Autowired
	private UsuarioRepository usuarioRepository;
	
	public Optional<UsuarioModel> cadastrarUsuario (UsuarioModel usuarioModel) {
		
		if (usuarioRepository.findByUsuario(usuarioModel.getUsuario()).isPresent())
			return Optional.empty();
		
		usuarioModel.setSenha(criptografarSenha(usuarioModel.getSenha()));
		
		return Optional.of(usuarioRepository.save(usuarioModel));
		
	}
	
	public Optional<UsuarioModel> atualizarUsuario(UsuarioModel usuarioModel) {
		
		if (usuarioRepository.findById(usuarioModel.getId()).isPresent()) {
			
			Optional<UsuarioModel> buscaUsuario = usuarioRepository.findByUsuario(usuarioModel.getUsuario());
			
			if ((buscaUsuario.isPresent()) && (buscaUsuario.get().getId() != usuarioModel.getId()))
				throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Usuário já existe", null);
			
			usuarioModel.setSenha(criptografarSenha(usuarioModel.getSenha()));
			
			return Optional.ofNullable(usuarioRepository.save(usuarioModel));
		}
		
		return Optional.empty();
	}
	
	public Optional<UsuarioLogin> autenticarUsuario(Optional<UsuarioLogin> usuarioLogin){
		
		Optional<UsuarioModel> usuario = usuarioRepository.findByUsuario(usuarioLogin.get().getUsuario());
		
		if (usuario.isPresent()) {
			
			if (compararSenhas(usuarioLogin.get().getSenha(), usuario.get().getSenha())) {
				
				usuarioLogin.get().setId(usuario.get().getId());
				usuarioLogin.get().setNome(usuario.get().getNome());
				usuarioLogin.get().setFoto(usuario.get().getFoto());
				usuarioLogin.get().setToken(gerarBasicToken(usuarioLogin.get().getUsuario(), usuarioLogin.get().getSenha()));
				usuarioLogin.get().setSenha(usuario.get().getSenha());
				
				return usuarioLogin;
			}
			
		}
		return Optional.empty();
	}
	
	private String criptografarSenha(String senha) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		return encoder.encode(senha);
	}
	
	private boolean compararSenhas(String senhaDigitada, String senhaBanco) {
		BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
		
		return encoder.matches(senhaDigitada, senhaBanco);
	}
	
	private String gerarBasicToken(String usuario, String senha) {
		String token = usuario + ":" + senha;
		byte[] tokenBase64 = Base64.encodeBase64(token.getBytes(Charset.forName("US-ASCII")));
		return "Basic " + new String(tokenBase64);
	}
}
