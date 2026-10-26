package com.fiap.techchallenge.security;

import com.fiap.techchallenge.usuario.domain.Usuario;
import com.fiap.techchallenge.usuario.usecase.UsuarioGateway;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UsuarioGateway usuarioGateway;

    public UserDetailsServiceImpl(UsuarioGateway usuarioGateway) {
        this.usuarioGateway = usuarioGateway;
    }

    @Override
    public UserDetails loadUserByUsername(String login) throws UsernameNotFoundException {
        Usuario usuario = usuarioGateway.findByLogin(login)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + login));
                
        return User.builder()
                .username(usuario.getLogin())
                .password(usuario.getSenhaHash())
                .roles(usuario.getRole().name())
                .build();
    }

}
