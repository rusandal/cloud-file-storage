package ru.minikhanov.cloud_storage.Utils;

/*
public class WithMockCustomUserSecurityContextFactory implements WithSecurityContextFactory<WithMockCustomUser> {

    @Override
    public SecurityContext createSecurityContext(WithMockCustomUser customUser) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        User user = MockUserUtils.getMockUser(customUser.username());
        UserDetailsImpl userDetails = UserDetailsImpl.build(user);
        Authentication auth = new UsernamePasswordAuthenticationToken(userDetails, user.getPassword(), userDetails.getAuthorities());
        context.setAuthentication(auth);
        return context;
    }

}
*/
